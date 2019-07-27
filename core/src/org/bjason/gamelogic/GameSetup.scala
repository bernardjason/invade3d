package org.bjason.gamelogic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle

object GameSetup {

  var _playerId = "-1"
  var _gameName:String = ""

  val GAME="Invade 3D"

  lazy val playerPrefix =  _playerId
  lazy val gameName =  _gameName

  private def _fancyFont = {

    val fontFile = Gdx.files.internal("data/OpenSans-Italic.ttf")
    val generator = new FreeTypeFontGenerator(fontFile);
    val parameter = new FreeTypeFontParameter();
    parameter.borderColor = Color.YELLOW
    parameter.shadowColor = new Color(0.8f,0.8f,0,1)
    parameter.borderWidth=5
    parameter.spaceX=5
    parameter.color=Color.RED
    parameter.shadowOffsetX=6

    parameter.size=100

    val font = generator.generateFont(parameter)
    generator.dispose();
    font
  }

  def defaultFont(size:Int,colour:Color) = {
    val fontFile = Gdx.files.internal("data/OpenSans-Italic.ttf")
    val generator = new FreeTypeFontGenerator(fontFile);
    val parameter = new FreeTypeFontParameter();
    parameter.color=Color.WHITE

    parameter.size=size

    val font = generator.generateFont(parameter)
    generator.dispose();
    font
  }

  lazy val fancyFont = _fancyFont

  def _getTitle = {
    val ls = new LabelStyle()
    ls.font = GameSetup.fancyFont
    val title= new Label( GameSetup.GAME,ls)

    val action = new Action() {
      var xxx=0f
      var yyy=0f
      var xdirection=1
      var ydirection= 1.5f
      var delayMove=60

      override def act(delta: Float): Boolean =  {
        if ( delayMove > 0 ) {
          xxx=actor.getX
          yyy=actor.getY
          delayMove=delayMove -1
        }
        this.actor.setX(xxx)
        this.actor.setY(yyy)
        this.actor.setZIndex(100)
        xxx=xxx+xdirection
        if ( xxx <= 0 || xxx+ this.actor.getWidth >= Gdx.graphics.getWidth ) xdirection= xdirection * -1
        yyy=yyy+ydirection
        if ( yyy <= 0 || yyy+ this.actor.getHeight >= Gdx.graphics.getHeight ) ydirection= ydirection * -1

        false
      }
    }
    title.addAction(action)

    title
  }
  lazy val getTitle = _getTitle
}
