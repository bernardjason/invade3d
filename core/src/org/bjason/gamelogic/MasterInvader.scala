package org.bjason.gamelogic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.{Environment, Model, ModelBatch}
import com.badlogic.gdx.math.{Matrix4, Quaternion, Vector3}
import org.bjason.gamelogic
import org.bjason.gamelogic.Log.{debug, info}
import org.bjason.gamelogic.basic.move.{AlienMissileMovement, MissileMovement, Movement}
import org.bjason.gamelogic.basic.shape
import org.bjason.gamelogic.basic.shape.{AlienMissileShape, Basic, CollideShape, FuelBase, NeverCollide, PlayerSprite}
import org.bjason.socket.{GameMessage, JsonObject, State, Websocket}

import scala.collection.mutable.ArrayBuffer

case class MinionStatusValue(var value: Int) {
  def dead() = value = 0

  def alive() = value = 1

  alive

  def isAlive = value > 0
}

class MasterInvader(val startPosition: Vector3 = new Vector3, val radius: Float = 16f,
                    override val id: String = gamelogic.basic.shape.Basic.getId, val remoteMaster: Boolean = false) extends Basic with Movement {

  val MASTER = "C_MASTER"
  //override val id: String = MASTER


  lazy val shape: CollideShape = new NeverCollide

  lazy val genModel = gamelogic.Common.assets.get("data/8_bit_space_ivader.g3db", classOf[Model])
  val rollbackScale = -2f
  val className = this.getClass.getSimpleName
  lazy val jsonObjectInstance = new JsonObject(className, id, gamelogic.Common.UNCHANGED, Some(State.ALIVE), instance = instance.transform)


  override lazy val jsonObject = Some(jsonObjectInstance)

  override var movement: Movement = this
  val RANGE_X = 128
  val RANGE_Z = 128
  val MOVE_DOWN_BY = 32
  var x = 1
  var z = 0
  var y = 0

  val move = new Vector3
  var allFourCorners = Array(-1, -1, 0, 0)
  var moveDown = 0f
  val SPEED = 20
  var oldx = 0
  var oldz = 0
  var ticker = 0f
  var fireTicker = 0f

  val xxx = 4
  val zzz = 4
  val yyy = 4
  val minions = Array.ofDim[MinionInvader](yyy, zzz, xxx)
  var binary = BigInt(0)
  val space = 64
  val offset = space * xxx * -0.5f
  val ALIVE = 1
  val DEAD = 0

  val minionsEnabled = true

  val minionStatus = Array.fill[MinionStatusValue](yyy, zzz, xxx)(MinionStatusValue(ALIVE))

  if (minionsEnabled && remoteMaster == false) createMinions

  private def createMinions = {
    for (yi <- 0 until yyy) {
      for (zi <- 0 until zzz) {
        for (xi <- 0 until xxx) {
          val pos = startPosition.cpy
          setOffsetMinionPosition(yi, zi, xi, pos)
          val m = new MinionInvader(minionStatus(yi)(zi)(xi), pos)
          Controller.addNewBasic(m)
          minions(yi)(zi)(xi) = m
        }
      }
    }
  }

  private def setOffsetMinionPosition(yi: Int, zi: Int, xi: Int, pos: Vector3) = {
    pos.x = pos.x + xi * space + offset
    pos.y = pos.y + yi * space + offset
    pos.z = pos.z + zi * space + offset
  }


  def zeroCorners = for (i <- 0 until allFourCorners.length) allFourCorners(i) = 0

  override def move(objects: List[Basic], me: Basic) = {
    ticker = ticker + (Gdx.graphics.getDeltaTime * 100)
    fireTicker = fireTicker + (Gdx.graphics.getDeltaTime )

    //val older = (for (j <- Websocket.jsonobjects if j.age > ticker) yield j).filter(j => j.what == className).sortWith(_.age < _.age)
    var older: JsonObject = null
    val allMasters = new ArrayBuffer[MasterInvader]()
    for (o <- Controller.objects) {
      val j = o.jsonObject.map { j =>
        if (o.isInstanceOf[MasterInvader]) allMasters += o.asInstanceOf[MasterInvader]

        if (j.age > ticker || (older != null && j.age > older.age)) {
          older = j
          println(o.id, "   ", j.what, "   ", j.age)
        }
      }
    }
    for (m <- allMasters) {
      if (m.id != id && m.jsonObjectInstance != null && m.jsonObjectInstance.other != null ) {
        val intList = m.jsonObjectInstance.other.split("!").toList // .map(_.toInt)
        val oBinary = BigInt(intList(7))
        processBinaryStatus(oBinary)
      }
    }

    if (older != null) {
      println(s"OLDER THAN me ${me.id}/${ticker} is ${older.id}/${older.age} ", " pos=", me.position)
      older.remoteUpdate(me)
      println(s"OLDER THAN me ${me.id} now ", " pos=", me.position)
      x = 0;
      y = 0;
      z = 0;
      val intList = older.other.split("!").toList // .map(_.toInt)
      println(older.other, "   split= ", intList)
      x = intList(0).toInt
      y = intList(1).toInt
      z = intList(2).toInt
      allFourCorners(0) = intList(3).toInt
      allFourCorners(1) = intList(4).toInt
      allFourCorners(2) = intList(5).toInt
      allFourCorners(3) = intList(6).toInt
      binary = BigInt(intList(7))

      processBinaryStatus(binary)

      ticker = older.age
    } else {
      moveAsNewest
      val speed = SPEED * Gdx.graphics.getDeltaTime
      move.set(x, y, z).scl(speed)
      me._translate(move)
    }


    if (minionsEnabled) moveMinions

  }

  private def fireAtBase(me: Vector3) = {

    val targetList = Controller.objects.filter{
      o =>
        o match {
          case o:FuelBase => true
          case o:PlayerSprite => true
          case _ =>false
        }
    }

    val whichOne=(Math.random() * 1000 % targetList.size).toInt

    val target = targetList(whichOne).position
    val startHere = me.cpy()
    val direction = startHere.sub(target).nor().scl(-1)
    val m = new AlienMissileMovement(direction = direction)
    val b = new AlienMissileShape(me.cpy, movement = m)
    m.objectToControl = b
    gamelogic.Controller.addNewBasic(b)
    Websocket.jsonobjects += b.jsonObject.get
  }

  var fire=false
  def processBinaryStatus(binaryIn: BigInt) = {

    var bitPosition = 0
    for (yi <- 0 until yyy) {
      for (zi <- 0 until zzz) {
        for (xi <- 0 until xxx) {
          val minion = minions(yi)(zi)(xi)
          var bit = BigInt(1)
          bit = bit << bitPosition
          val alive = binaryIn & bit
          if (alive == 0) { // && minion.minionStatus.isAlive) {
            minion.minionStatus.dead()
            Controller.addToDead(minion)
          }
          bitPosition = bitPosition + 1

        }
      }
    }
  }

  private def moveAsNewest = {
    jsonObjectInstance.age = ticker.toInt
    jsonObjectInstance.other = s"${x}!${y}!${z}!${allFourCorners(0)}!${allFourCorners(1)}!${allFourCorners(2)}!${allFourCorners(3)}!${binary}"

    moveDownIfAllFourCornersVisited

    if (y == 0) {
      if (x > 0 && position.x > RANGE_X) {
        z = -1;
        x = 0
        allFourCorners(0) = allFourCorners(0) + 1
      }
      if (z < 0 && position.z < -RANGE_Z) {
        x = -1;
        z = 0
        allFourCorners(1) = allFourCorners(1) + 1
      }
      if (x < 0 && position.x < -RANGE_X) {
        z = 1;
        x = 0
        allFourCorners(2) = allFourCorners(2) + 1
      }
      if (z > 0 && position.z > RANGE_Z) {
        x = 1;
        z = 0
        allFourCorners(3) = allFourCorners(3) + 1
      }
    } else {
      x = 0
      z = 0
      moveDown = moveDown - 1
      if (moveDown < 0) {
        y = 0;
        x = oldx;
        z = oldz
        zeroCorners
      }
    }
  }

  private def moveDownIfAllFourCornersVisited = {
    if (allFourCorners.sum == 4) {
      zeroCorners
      moveDown = MOVE_DOWN_BY
      oldx = x
      oldz = z
      y = -1
    }
  }

  private def moveMinions = {
    var bitPosition = 0
    binary = 0

    if (fireTicker % 10 < 0.1  ) {
      fireTicker=fireTicker+1
      fire=true
    }
    var random=Math.random() * 1000 % 10

    for (yi <- yyy-1 to 0 by -1) {
      for (zi <- 0 until zzz) {
        for (xi <- 0 until xxx) {
          val minion = minions(yi)(zi)(xi)
          var bit = BigInt(0)
          if (minion.minionStatus.isAlive) {
            bit = 1
            val pos = minion.position.set(position)
            setOffsetMinionPosition(yi, zi, xi, pos)
            minion._getTransform().setToTranslation(pos)
            if ( fire ) {
              random=random-1
              if ( random <= 0 ) {
                fireAtBase(pos)
                fire=false
              }
            }
          } else if (minion.onScreen) {
            Controller.addToDead(minion)
          }
          bit = bit << bitPosition
          binary = binary | bit
          bitPosition = bitPosition + 1
        }
      }
    }

    //Websocket.sendMessage(GameMessage(id = MASTER, msg = "hello"))
  }

  override def receiveMessage(message: GameMessage): Unit = {
    super.receiveMessage(message)
    if (message.id == "MASTER") {
      debug("MESSAGE RECEIVED !!!!!!!!!!!!!!!!!", message)
    } else {
    }
  }

  override def move(objects: List[Basic]) = {
    if (this.id == Controller.master.id) movement.move(objects, this)
  }

  override def _render(modelBatch: ModelBatch, environment: Environment, cam: Camera): Unit = {
    super._render(modelBatch, environment, cam)

  }
}
