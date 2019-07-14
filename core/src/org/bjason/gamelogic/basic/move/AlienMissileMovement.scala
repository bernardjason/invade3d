package org.bjason.gamelogic.basic.move

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.shape
import org.bjason.gamelogic.basic.shape.{FuelBase, PlayerSprite}
import org.bjason.gamelogic.{GameInformation, MasterInvader, MinionInvader}
import org.bjason.socket.{GameMessage, Websocket}

case class AlienMissileMovement(direction: Vector3, val speed: Float = 50f) extends Movement {

  private val translation = new Vector3
  var objectToControl:shape.Basic=null


  override def move(objects: List[shape.Basic], me: shape.Basic) {

    me.save()

    me.jsonObject.get.changed=gamelogic.Common.CHANGED

    translation.set(direction)
    translation.scl(Gdx.graphics.getDeltaTime() * speed )

    me._translate(translation)
    if ( me.position.y < 0 ) {
      gamelogic.Controller.addToDead(objectToControl)
    }
  }

  override def collision(me: shape.Basic, other:shape.Basic) {
    other match {
      case b: PlayerSprite => //gamelogic.Controller.addToDead(other)
        gamelogic.Sound.playScoop
        Websocket.broadcastMessage(GameMessage(msg = "Explosion", objMatrix4 = me.instance.transform))
        GameInformation.addScore(1)
        info(s"Missile exploded!!!!!!! ${me}")
        Websocket.broadcastMessage( GameMessage(msg = "Explosion",objId = gamelogic.GameSetup.playerPrefix.toString,objMatrix4 =  me.instance.transform))
        gamelogic.Explosion(me.position)
        translation.setZero()
        gamelogic.Controller.addToDead(objectToControl)
        explode(objectToControl)
      case b: FuelBase => //gamelogic.Controller.addToDead(other)
        gamelogic.Sound.playScoop
        Websocket.broadcastMessage(GameMessage(msg = "Explosion", objMatrix4 = me.instance.transform))
        gamelogic.Explosion(me.position)
        gamelogic.Controller.addToDead(objectToControl)
        b.jsonObject.get.dead
      case b: MinionInvader => //gamelogic.Controller.addToDead(other)
      case b: MasterInvader => //gamelogic.Controller.addToDead(other)
      case _ =>
    }
  }

}