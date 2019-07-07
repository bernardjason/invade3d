package org.bjason.gamelogic

import com.badlogic.gdx.graphics.{Color, GL20}
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.{ApplicationAdapter, Gdx}
import org.bjason.gamelogic

case class EndScreen() extends ApplicationAdapter {

  lazy val stage = new Stage()

  lazy val skin = new Skin(Gdx.files.internal("data/uiskin.json"))

  override def create(): Unit = {

    Gdx.input.setInputProcessor(stage)
    val table = new Table

    table.setFillParent(true)
    table.setDebug(true)
    stage.addActor(table)

    val default= gamelogic.GameSetup.defaultFont(30,Color.WHITE)
    val defaultScoreTitle = gamelogic.GameSetup.defaultFont(40,Color.WHITE)
    skin.get(classOf[TextButton.TextButtonStyle]).font = default

    val sep = new Label("",skin,"TITLE")

    table.setDebug(false) // This is optional, but enables debug lines for tables.

    val title = gamelogic.GameSetup.getTitle
    table.add(title).colspan(2).align(Align.center).padBottom(20)
    table.row()
    table.add(sep)
    table.row()

    table.add( new Label( "Scores...",new LabelStyle(defaultScoreTitle,Color.FIREBRICK)))
    table.row()

    val pad = 30

    var first=true
    for(s <- GameInformation.allScoreFinal) {
      if ( first ) {
        table.add( new Label( s,new LabelStyle(default,Color.CYAN))).align(Align.left).padLeft(pad)
        first=false
      } else {
        table.add( new Label( s,new LabelStyle(default,Color.WHITE))).align(Align.left).padLeft(pad)
      }
      table.row()
    }
    table.add( new Label( "",skin))
    table.row()

    val end = new TextButton("End", skin)
    end.pad(10)
    table.add(end).pad(10).colspan(2).width(150)

    end.addListener(new ChangeListener() {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        System.exit(0)
      }
    })
  }

  override def resize(width: Int, height: Int): Unit = {
    stage.getViewport.update(width, height, true)
  }

  override def render(): Unit = {
    Gdx.gl.glClearColor( 0, 0, 0, 1 );

    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    stage.act(Gdx.graphics.getDeltaTime)
    stage.draw
  }

  override def dispose(): Unit = {
    stage.dispose
  }


}
