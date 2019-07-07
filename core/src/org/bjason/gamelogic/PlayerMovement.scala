package org.bjason.gamelogic

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.{Matrix4, Quaternion, Vector3}
import com.badlogic.gdx.{Gdx, InputAdapter}
import org.bjason.gamelogic
import org.bjason.gamelogic.Log.info
import org.bjason.gamelogic.basic.move.{MissileMovement, Movement}
import org.bjason.gamelogic.basic.shape
import org.bjason.gamelogic.basic.shape.PlayerSprite
import org.bjason.socket.{GameMessage, Websocket}

object PlayerMovement  extends InputAdapter with Movement {

  val rotationAround = new Matrix4()
  var dir = 0f;
  var pitch=0f
  var appliedPitch=0f
  val GROUND=10

  private var fire = false
  val SENSITIVE=2
  var timeSinceLastFire = 0L
  val GAP_BETWEEN_FIRE = 200

  var speed = 0f;
  val gravity= new Vector3(0,-0.25f,0)

  def init(player:PlayerSprite) = {
    rotationAround.set(player.instance.transform)
    rotationAround.setTranslation(player.startPosition)
  }

  override def move(objects: List[shape.Basic], me: shape.Basic) {


    rotationAround.setTranslation(me.position)

    appliedPitch=appliedPitch+pitch
    rotationAround.rotate(0, 1, 0, dir)
    me._getTransform().set(rotationAround)
    me._getTransform().rotate(1,0,0,appliedPitch)

    me.save()

    if ( speed != 0 ) {
      me._translate(0,2,0)
    }
    me._trn(gravity)

    me.jsonObject.get.changed=gamelogic.Common.CHANGED

    me._getTransform().getTranslation(me.position)

    if ( me.position.y <= GROUND ) me.rollback

    me.collisionCheck(objects,true)


    if ( isFireAndUnset ) {
      val q = new Quaternion()
      me._getRotation(q)

      val startHere = me.position.cpy()
      val add = new Vector3(0,0,-1)
      q.transform(add)
      add.scl(15)
      startHere.add(add)

      val dir = new Vector3(0,0,-1)
      val m = new MissileMovement(direction=dir)
      val b = new shape.MissileShape(startHere, movement =m)

      b._getTransform().set(q)

      m.objectToControl=b
      gamelogic.Controller.addNewBasic(b)
      Websocket.jsonobjects += b.jsonObject.get
      gamelogic.Sound.playFire
    }
  }

  override def collision(me: shape.Basic, other:shape.Basic) {

    GameInformation.playerHit()
    gamelogic.Sound.playHit
    other match {
      case b:MinionInvader => // gamelogic.Controller.addToDead(other)
        GameInformation.addScore(1)
        Websocket.broadcastMessage( GameMessage(msg = "Explosion",objMatrix4 =  me.instance.transform))
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
    false;
  }

  override def keyDown(c: Int): Boolean = {
    if (Gdx.input.isKeyPressed(Keys.LEFT)) {
      dir=1
    }
    if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
      dir = -1;
    }
    if (Gdx.input.isKeyPressed(Keys.UP)) {
      pitch = 1
    }
    if (Gdx.input.isKeyPressed(Keys.DOWN)) {
      pitch =  -1
    }

    if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) == true) {
      speed = 1f
    }
    if (Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT) == true) {
      speed = -1f
    }
    if (Gdx.input.isKeyPressed(Keys.SPACE) == true) {
      fire = true
    }
    if (Gdx.input.isKeyPressed(Keys.ESCAPE) == true) {
      System.exit(0)
    }

    false
  }
}
