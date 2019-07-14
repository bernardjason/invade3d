package org.bjason.socket

import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue}

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.{Done, NotUsed}
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net.{HttpRequest, HttpResponse, HttpResponseListener}
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter.OutputType
import com.typesafe.config.ConfigFactory
import org.bjason.gamelogic
import org.bjason.gamelogic.Controller.ticker
import org.bjason.gamelogic.Log.info
import org.bjason.gamelogic.basic.shape.Basic

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

/*
Sec-WebSocket-Protocol: json
 */
object Websocket {

  case class Login(prefix: String, name: String, game: String)

  val heroku = ! Gdx.files.external("gamelogic_local.txt").exists()
  var URL = "localhost:9000"
  var PROTOCOL = "ws"
  var TESTPROTOCOL = "http"

  if (heroku) {
    PROTOCOL = "wss"
    TESTPROTOCOL = "https"
    URL = "landedblocked.herokuapp.com"
  } else {
    URL = "localhost:9000"
    PROTOCOL = "ws"
    TESTPROTOCOL = "http"
  }

  val SCORE = "SCORE"

  var clientActor: ActorRef = null

  val entries: ConcurrentHashMap[String, JsonObject] = new ConcurrentHashMap[String, JsonObject]
  val messages: ConcurrentLinkedQueue[GameMessage] = new ConcurrentLinkedQueue[GameMessage]

  def end {
    clientActor ! PoisonPill
  }

  def connect(player: Login) = {

    val config = ConfigFactory.load("akka.conf")
    implicit val system = ActorSystem("bern",config)
    implicit val materializer = ActorMaterializer()

    import system.dispatcher

    val req = WebSocketRequest(uri = s"${
      PROTOCOL
    }://${
      URL
    }/ws")
    //val req = WebSocketRequest(uri = "ws://localhost:9000/ws")
    //val req = WebSocketRequest(uri = "wss://bjason-multicopter.herokuapp.com/ws")
    val webSocketFlow = Http().webSocketClientFlow(req)

    val messageSource: Source[Message, ActorRef] =
      Source.actorRef[TextMessage.Strict](bufferSize = 4096, OverflowStrategy.fail)


    val messageSink: Sink[Message, NotUsed] =
      Flow[Message]
        .map {
        message =>
          val json = new Json()
          val msg = message.asTextMessage.getStrictText

          if (msg.contains("msg")) {
            info("MESSAGE Websocket received " + msg)
            val jo = json.fromJson(classOf[GameMessage], msg)
            messages.add(jo)
          } else {
            val jo = json.fromJson(classOf[JsonObject], msg)
            entries.put(jo.id, jo)
            val WITHOUT_THIS_ALWAYS_DEFAULT_MATRIX = jo.instance.getScaleX
          }

      }.to(Sink.ignore)


    val ((ws, upgradeResponse), closed) =
      messageSource
        .viaMat(webSocketFlow)(Keep.both)
        .toMat(messageSink)(Keep.both)
        .run()


    val connected = upgradeResponse.flatMap {
      upgrade =>
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          Future.successful(Done)
        } else {
          throw new RuntimeException(s"Connection failed: ${
            upgrade.response.status
          }")
        }
    }

    clientActor = ws


    val json = new Json(OutputType.json)
    val j = json.toJson(player, player.getClass)
    clientActor ! TextMessage(j)

  }

  def send(s: String) {
    clientActor ! TextMessage(s)
  }

  def test: Unit = {
    val httpGet = new HttpRequest("GET");
    httpGet.setUrl(s"${Websocket.TESTPROTOCOL}://${Websocket.URL}/");
    Gdx.net.sendHttpRequest(httpGet, new HttpResponseListener() {
      override def handleHttpResponse(response: HttpResponse) {
        val status = response.getResultAsString();
        println(status)

      }

      override def failed(t: Throwable) {
      }

      override def cancelled(): Unit = {}
    });
  }


  val jsonobjects: ArrayBuffer[JsonObject] = ArrayBuffer()

  def sayHello = {
    send("{\"HELLO\":\"SEND\"}")
  }

  private val deleteList: ArrayBuffer[JsonObject] = ArrayBuffer()
  def writeAllMyObjects(): Unit = {
    if (ticker % 4 == 0) {
      var sent = 0

      for (o <- jsonobjects.filter(f => f.changed > gamelogic.Common.UNCHANGED || f.id.startsWith(Basic.playerPrefix) || f.state.get == State.DEAD)) {

        val json = new Json(OutputType.json)
        val j = json.toJson(o, o.getClass)

        Websocket.send(j)
        sent = sent + 1
        if (o.state.get == State.DEAD) {
          deleteList += o
        }
      }
      for (j <- jsonobjects) {
        if (j.state.get == State.DEAD) {
          deleteList += j
        }
      }
      //info("Sent "+sent)
    }
    //dumpJsonObjects()
  }
  def clearDeletedObjects = {
    for (d <- deleteList) {
      d.whenDeadLinger=d.whenDeadLinger -1
      if ( d.whenDeadLinger < 0 ) {
        jsonobjects -= d
        info("REMOVED ", d)
      }
    }

    deleteList.clear()

  }

  def dumpJsonObjects(force:Boolean = false) = {
    if (ticker % 90 == 0 || force ) {
      info("jsonObjects " + jsonobjects.size)
      for (j <- jsonobjects) {
        info(s"${j.what}, ${j.id}, ${j.age} ${j.state}");
      }
    }
  }

  def readOtherObjects() = {
    import scala.collection.JavaConversions._
    for (jo <- Websocket.entries.values()) {
      jo.toObject()
    }
    var messages: Array[GameMessage] = Array()
    messages = Websocket.messages.toArray(messages)
    for (m <- messages) {
      if (m.msg.startsWith("GAME_OVER")) {
        gamelogic.GameInformation.end = true
      } else if (m.msg.startsWith(SCORE)) {
        gamelogic.GameInformation.gotScore(m)
      } else if (m.msg.startsWith("Explosion") && m.objId != gamelogic.GameSetup.playerPrefix.toString && m.objMatrix4 != null ) {
        val position = new Vector3
        m.objMatrix4.getTranslation(position)
        gamelogic.Explosion(position)
      } else {
        var found = false
        for (o <- gamelogic.Controller.objects) {
          if (o.id == m.id) {
            o.receiveMessage(m)
          }
        }
        if (!found) {
          for (t <- gamelogic.Controller.terrains.values) {
            for (o <- t.objects) {
              if (o.id == m.objId) {
                o.receiveMessage(m)
              }
            }
          }
        }

      }
      Websocket.messages.remove(m)
    }
  }

  def broadcastMessage(text: String): Unit = {
    sendMessage("0", text)
  }

  def broadcastMessage(g: GameMessage): Unit = {
    g.id = "0"
    val json = new Json(OutputType.json)
    val j = json.toJson(g, g.getClass)
    Websocket.send(j)
  }

  def sendMessage(g: GameMessage): Unit = {
    val json = new Json(OutputType.json)
    val j = json.toJson(g, g.getClass)
    Websocket.send(j)
  }

  def sendMessage(id: String, text: String) {
    val m = GameMessage(id, s"${id} ${text}")
    val json = new Json(OutputType.json)
    val j = json.toJson(m, m.getClass)
    Websocket.send(j)
  }

}
