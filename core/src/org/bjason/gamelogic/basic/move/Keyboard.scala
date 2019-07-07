package org.bjason.gamelogic.basic.move

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.{Quaternion, Vector3}
import org.bjason.gamelogic
import org.bjason.gamelogic.{GameInformation, InputHandler, basic}
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.shape
import org.bjason.socket.{GameMessage, Websocket}

object Keyboard extends Movement {

  val GROUND=10
  val speed = 220
  val translation = new Vector3()
  override def move(objects: List[shape.Basic], me: shape.Basic) {

    me.save()

    translation.set(InputHandler.userAction)

    translation.scl( -speed * Gdx.graphics.getDeltaTime())

    me.jsonObject.get.changed=gamelogic.Common.CHANGED

    val b4 = me._getTransform().cpy()
    val b4p = me.position
    me._trn(translation)
    translation.setZero()
    if ( me.position.x > shape.Terrain.MAXX) {
      translation.x= -shape.Terrain.MAXX*2
    }
    if ( me.position.z > shape.Terrain.MAXZ) {
      translation.z= -shape.Terrain.MAXZ*2
    }
    if ( me.position.x < -shape.Terrain.MAXX) {
      translation.x= shape.Terrain.MAXX*2
    }
    if ( me.position.z < -shape.Terrain.MAXZ) {
      translation.z= shape.Terrain.MAXZ*2
    }
    if ( ! translation.isZero ) {
      me._trn(translation)
    }

    if ( me.position.y > GROUND ) {
      me._rotate(0,1,0,InputHandler.dir)
    } else {
      InputHandler.rollback
    }
    if ( me.position.y < GROUND  ) {
      me._getTransform().set(b4)
      me.position.set(b4p)
    }

    me.collisionCheck(objects,true)


    if ( InputHandler.isFireAndUnset ) {
      val q = new Quaternion()
      me._getRotation(q)
      val startHere = me.position.cpy()
      startHere.mulAdd(InputHandler.direction,-20f)
      val dir = new Vector3(0,0,-1)
      val m = new MissileMovement(direction=dir)
      val b = new shape.MissileShape(startHere.cpy, movement =m)
      b._getTransform().set(q)
      m.objectToControl=b
      gamelogic.Controller.addNewBasic(b)
      Websocket.jsonobjects += b.jsonObject.get
      gamelogic.Sound.playFire
    }
  }

  override def collision(me: shape.Basic, other:shape.Basic) {
    InputHandler.userAction.setZero()

    GameInformation.playerHit()
    gamelogic.Sound.playHit
    other match {
      case b:shape.EightBitInvader => gamelogic.Controller.addToDead(other)
        GameInformation.addScore(1)
        Websocket.broadcastMessage( GameMessage(msg = "Explosion",objMatrix4 =  me.instance.transform))
      case _ =>
        me.rollback
        InputHandler.rollback
        InputHandler.stopPlayer
    }
    info(s"COLLISION ${me.position}")
  }
}