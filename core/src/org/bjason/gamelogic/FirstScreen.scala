package org.bjason.gamelogic

import java.io.IOException

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type
import com.badlogic.gdx.scenes.scene2d._
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter.DigitsOnlyFilter
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.{ApplicationAdapter, Gdx}
import org.bjason.gamelogic
import org.bjason.socket.Websocket

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

case class FirstScreen() extends ApplicationAdapter {

  lazy val stage = new Stage()

  lazy val skin = new Skin(Gdx.files.internal("data/uiskin.json"))

  var playerId = -1
  var gameName = ""
  var start: TextButton = null
  var mainGame: MainGame = null
  var launchState = 0

  def handleDefaults = {

    val playerIdFile = Gdx.files.external("._playerid.txt")

    if (playerIdFile.exists()) {
      val contents = playerIdFile.readString().trim
      try {
        playerId = contents.toInt
      } catch {
        case _: Throwable =>
      }
    }
    val gameNameFile = Gdx.files.external("._game.txt")
    if (gameNameFile.exists()) {
      val contents = gameNameFile.readString().trim
      gameName = contents
    }
  }

  def writeDefaults = {
    val playerIdFile = Gdx.files.external("._playerid.txt")
    playerIdFile.writeString(s"${playerId}", false)
    val gameNameFile = Gdx.files.external("._game.txt")
    gameNameFile.writeString(s"${gameName}", false)

  }

  override def create(): Unit = {

    handleDefaults

    Gdx.input.setInputProcessor(stage)
    val table = new Table

    table.setFillParent(true)
    table.setDebug(true)
    stage.addActor(table)

    val default = gamelogic.GameSetup.defaultFont(30, Color.WHITE)
    skin.get(classOf[TextButton.TextButtonStyle]).font = default
    skin.get(classOf[TextField.TextFieldStyle]).font = default

    val sep = new Label("", skin, "TITLE")
    table.setDebug(false) // This is optional, but enables debug lines for tables.

    val title = gamelogic.GameSetup.getTitle

    table.add(title).colspan(2).align(Align.center).padBottom(20)
    table.row()
    table.add(sep).colspan(2)
    table.row()

    val textTable = new Table
    val padding = 10

    val style = new LabelStyle(default, Color.WHITE)

    textTable.add(new Label("(Digits only) player id", style)).align((Align.right)).pad(padding)
    val playerIdWidget = if (playerId > 0) new TextField("" + playerId, skin) else new TextField("", skin)
    playerIdWidget.setTextFieldFilter(new DigitsOnlyFilter)
    textTable.add(playerIdWidget).align(Align.left)
    textTable.row()

    textTable.add(new Label("Game name", style)).align(Align.right).pad(padding)
    val gameIdWidget = new TextField(gameName, skin)
    textTable.add(gameIdWidget).align(Align.left)
    textTable.row()

    table.add(textTable).colspan(2)
    table.row()
    table.add(sep).colspan(2)
    table.row()

    val buttonWidth = 100
    start = new TextButton("Start", skin)
    start.pad(20)
    table.add(start).pad(5).align(Align.right).width(buttonWidth)


    val cancel = new TextButton("Cancel", skin)
    cancel.pad(20)
    table.add(cancel).pad(5).align(Align.left).width(buttonWidth)

    table.row()
    table.add(sep).colspan(2).padTop(20)
    table.row()


    val http = s"${Websocket.TESTPROTOCOL}://${Websocket.URL}"
    //val serverAddr = new Label(s"Server $http", skin)
    val serverAddr = new TextButton(s"$http", skin)
    serverAddr.addListener(new ChangeListener {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        Try {
          Gdx.net.openURI(http)
        }
      }
    })
    serverAddr.setColor(Color.BLACK)
    serverAddr.getLabel.setColor(Color.CYAN)
    serverAddr.getStyle.overFontColor = Color.BLUE
    serverAddr.pad(0)


    table.add(new Label("Server address ", skin)).align((Align.right)).colspan(1).pad(0).space(0)
    table.add(serverAddr).align((Align.bottomLeft)).colspan(2).pad(0).space(0)
    table.row().pad(0).space(0)

    table.add(sep).pad(0).space(0)
    table.add(new Label("Open in default browser", skin)).align((Align.topLeft)).pad(0).space(0)
    table.row().pad(0).space(0)

    def startGame = {
      playerId = -1
      gameName = null
      playerIdWidget.setColor(Color.BLACK)
      gameIdWidget.setColor(Color.BLACK)
      if (  playerIdWidget.getText.trim.matches("\\d\\d*") ) {
        playerId = playerIdWidget.getText.trim.toInt
      }
      if ( playerId <= 0 ) {
        playerIdWidget.setColor(Color.RED)
        playerIdWidget.setMessageText("Int > 0")
      }
      if ( gameIdWidget.getText.trim.length > 0 ) {
        gameName = gameIdWidget.getText.trim
      } else {
        gameIdWidget.setColor(Color.RED)
        gameIdWidget.setMessageText("name pls")
      }

      if ( playerId > 0 && gameName != null ) {
        val bar = new ProgressBar(0, 10, 1, false, skin)

        val window = new Window("Loading...", skin)
        window.setSize(400, 400)
        window.setPosition(Gdx.graphics.getWidth / 2 - 200, Gdx.graphics.getHeight / 2 - 200)
        val windowTable = new Table
        window.add(windowTable)
        val doingWhat = new Label("Currently doing....", skin)
        windowTable.add(doingWhat)
        windowTable.row()
        windowTable.add(bar)
        windowTable.row()
        stage.addActor(window)


        writeDefaults

        bar.setValue(0)

        launchState = 1

        val f = Future {

          for (i <- 1 to 10) {
            bar.setValue(i)

            Thread.sleep(100)
          }

        }
        gamelogic.GameSetup._gameName = gameName
        gamelogic.GameSetup._playerId = playerId
        mainGame = new MainGame

        f.onComplete {
          case Success(value) => launchState = 3
          case Failure(e) => e.printStackTrace
        }

      }

    }

    start.addListener(new ChangeListener() {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        startGame
      }
    })

    cancel.addListener(new ChangeListener() {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        System.exit(0)
      }
    })
    stage.addListener(new InputListener() {
      override def keyDown(event: InputEvent, keycode: Int) = {
        if (event.getType == Type.keyDown && keycode == Keys.ENTER) {
          Future {
            Thread.sleep(50)
            startGame
          }
        }
        false
      }
    }

    )
  }

  override def resize(width: Int, height: Int): Unit = {
    stage.getViewport.update(width, height, true)
  }

  override def render(): Unit = {
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    stage.act(Gdx.graphics.getDeltaTime)
    stage.draw

    launchState match {
      case 1 =>
      case 2 =>
      case 3 =>
        mainGame.create()
        GameProxy.setAdapter(mainGame)
      case _ =>
    }
  }

  override def dispose(): Unit = {
    stage.dispose
  }
}
