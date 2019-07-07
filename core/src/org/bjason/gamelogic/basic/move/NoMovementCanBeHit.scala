package org.bjason.gamelogic.basic.move

import org.bjason.gamelogic
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.shape
import org.bjason.socket.Websocket

object NoMovementCanBeHit extends Movement {

  override def move(objects: List[shape.Basic], me: shape.Basic) {

  }

  override def collision(me: shape.Basic, other:shape.Basic) {
    info(s"OBJECT ${me}  ${me.id} DEAD")
    me.dead=true
    gamelogic.Controller.addToDead(me)
    Websocket.dumpJsonObjects(true)
  }
}
