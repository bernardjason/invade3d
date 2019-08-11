package org.bjason.gamelogic.basic.move

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.{Controller, GameInformation, MinionInvader, basic}
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.shape
import org.bjason.socket.{GameMessage, Websocket}

case class MissileMovement(direction: Vector3, val speed: Float = 250f) extends Movement {

  private val translation = new Vector3
  var objectToControl:shape.Basic=null

  var ttl = 3f

  override def move(objects: List[shape.Basic], me: shape.Basic) {

    me.save()

    //if ( Controller.ticker % 200 == 0 ) me.jsonObject.get.changed=gamelogic.Common.CHANGED

    translation.set(direction)
    translation.scl(Gdx.graphics.getDeltaTime() * speed )

    me._translate(translation)
    ttl = ttl - Gdx.graphics.getDeltaTime
    if ( ttl < 0 ) {
      gamelogic.Controller.addToDead(objectToControl)
    }
  }

  override def collision(me: shape.Basic, other:shape.Basic) {
    other match {
      case b: MinionInvader => //gamelogic.Controller.addToDead(other)
        gamelogic.Sound.playScoop
        Websocket.broadcastMessage(GameMessage(msg = "Explosion", objMatrix4 = me.instance.transform))
        //gamelogic.Controller.addToDead(other)
        GameInformation.addScore(1)
      case _ =>
    }
    info(s"Missile exploded!!!!!!! ${me}")
    Websocket.broadcastMessage( GameMessage(msg = "Explosion",objId = gamelogic.GameSetup.playerPrefix.toString,objMatrix4 =  me.instance.transform))
    gamelogic.Explosion(me.position)
    translation.setZero()
    gamelogic.Controller.addToDead(objectToControl)
    explode(objectToControl)
  }

}