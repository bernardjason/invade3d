package org.bjason.gamelogic

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.{Matrix4, Quaternion, Vector3}
import com.badlogic.gdx.{Gdx, InputAdapter}
import org.bjason.gamelogic
import org.bjason.gamelogic.Log.info
import org.bjason.gamelogic.basic.move.{MissileMovement, Movement}
import org.bjason.gamelogic.basic.shape
import org.bjason.gamelogic.basic.shape.{AlienMissileShape, PlayerSprite}
import org.bjason.socket.{GameMessage, Websocket}

object PlayerMovement extends InputAdapter with Movement {

  val rotationAround = new Matrix4()
  var dir = 0f;
  var pitch = 0f
  var appliedPitch = 0f
  val GROUND = 10

  var keyDown=false

  private var fire = false
  val SENSITIVE = 2
  var timeSinceLastFire = 0L
  val GAP_BETWEEN_FIRE = 200

  var speed = 0f;
  val gravity = new Vector3(0, -0.25f, 0)

  def init(player: PlayerSprite) = {
    rotationAround.set(player.instance.transform)
    rotationAround.setTranslation(player.startPosition)
  }

  override def move(objects: List[shape.Basic], me: shape.Basic) {



    rotationAround.setTranslation(me.position)

    appliedPitch = appliedPitch + pitch
    rotationAround.rotate(0, 1, 0, dir)
    me._getTransform().set(rotationAround)
    me._getTransform().rotate(1, 0, 0, appliedPitch)

    me.save()

    if (speed != 0) {
      me._translate(0, 2, 0)
    }
    me._trn(gravity)

    me.jsonObject.get.changed = gamelogic.Common.CHANGED

    me._getTransform().getTranslation(me.position)

    if (me.position.y <= GROUND) me.rollback

    me.collisionCheck(objects, true)


    if (isFireAndUnset) {
      val q = new Quaternion()
      me._getRotation(q)

      val startHere = me.position.cpy()
      val add = new Vector3(0, 0, -1)
      q.transform(add)
      add.scl(15)
      startHere.add(add)

      val dir = new Vector3(0, 0, -1)
      val m = new MissileMovement(direction = dir)
      val b = new shape.MissileShape(startHere, movement = m)

      b._getTransform().set(q)

      m.objectToControl = b
      gamelogic.Controller.addNewBasic(b)
      Websocket.jsonobjects += b.jsonObject.get
      gamelogic.Sound.playFire
    }

    if ( keyDown == false ) {
      //speed=0
      dir=0
      pitch=0
    }
  }

  override def collision(me: shape.Basic, other: shape.Basic) {

    other match {
      case m: MinionInvader  =>
        GameInformation.addScore(1)
        Websocket.broadcastMessage(GameMessage(msg = "Explosion", objMatrix4 = me.instance.transform))
        gamelogic.Explosion(m.position)
        GameInformation.playerHit()
        gamelogic.Sound.playHit
      case  _: AlienMissileShape =>
        GameInformation.addScore(10)
        Websocket.broadcastMessage(GameMessage(msg = "Explosion", objMatrix4 = me.instance.transform))
        GameInformation.playerHit()
        gamelogic.Sound.playHit
      case _ =>
        me.rollback
    }
    info(s"COLLISION ${me.position}")
  }

  def isFireAndUnset = {
    var r = false
    if (fire) {
      val now = System.currentTimeMillis()
      if (now > timeSinceLastFire + GAP_BETWEEN_FIRE) {
        timeSinceLastFire = now
        r = true
      }
    }
    r
  }

  override def keyUp(c: Int): Boolean = {
    c match {
      case Keys.LEFT | Keys.RIGHT => dir = 0;
      case Keys.UP | Keys.DOWN => pitch = 0;
      case Keys.SHIFT_LEFT | Keys.SHIFT_RIGHT => speed = 0f
      case Keys.SPACE => fire = false
      case _ =>
    }
    if  ( dir== 0 && pitch == 0 && fire == false && speed == 0) {
      keyDown = false
    }
    false;
  }

  override def keyDown(c: Int): Boolean = {
    if (Gdx.input.isKeyPressed(Keys.LEFT)) {
      keyDown=true
      dir = 1
    }
    if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
      keyDown=true
      dir = -1;
    }
    if (Gdx.input.isKeyPressed(Keys.UP)) {
      keyDown=true
      pitch = 1
    }
    if (Gdx.input.isKeyPressed(Keys.DOWN)) {
      keyDown=true
      pitch = -1
    }

    if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) == true) {
      keyDown=true
      speed = 1f
    }
    if (Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT) == true) {
      keyDown=true
      speed = -1f
    }
    if (Gdx.input.isKeyPressed(Keys.SPACE) == true) {
      keyDown=true
      fire = true
    }
    if (Gdx.input.isKeyPressed(Keys.ESCAPE) == true) {
      System.exit(0)
    }

    false
  }

  var screenX = 0
  var screenY = 0

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = {
    //dir=0
    ///itch=0
    /*
    if (screenY < this.screenY) pitch = -1
    else if (screenY > this.screenY) pitch = 1
    else pitch = 0
    if (screenX < this.screenX) dir = 1
    else if (screenX > this.screenX) dir = -1
    else dir = 0
     */
    pitch = screenY - this.screenY
    dir = this.screenX - screenX

    this.screenX=screenX
    this.screenY=screenY
    true
  }

  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    println(pointer,button)
    if ( button == 0 ) speed = 1
    if ( button == 1 ) fire = true
    super.touchDown(screenX, screenY, pointer, button)
  }

  override def touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = {
    mouseMoved(screenX,screenY)
  }

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    if ( button == 0 ) speed = 0
    if ( button == 1 ) fire = false
    super.touchUp(screenX, screenY, pointer, button)
  }
}
