package org.bjason.gamelogic.basic.move

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.shape
import org.bjason.socket.{GameMessage, Websocket}
case class InvaderMovement(var speed: Float = 65f) extends Movement {

  private val translation = new Vector3
  var objectToControl: shape.Basic = null

  val direction = new Vector3(0, -1, 0)
  val START_AGAIN=3
  var maxHeight=100f
  val MIN_HEIGHT=5f
  var minHeight=MIN_HEIGHT
  var landed =  10
  var onLand = false
  var lastLandedWas = "xxx"
  var didILandOnLand=false
  var broadcast=true

  override def move(objects: List[shape.Basic], me: shape.Basic) {
    if ( broadcast == false ) me.jsonObject.get.changed=gamelogic.Common.UNCHANGED

    if ( broadcast ) {
      me.jsonObject.get.changed=gamelogic.Common.CHANGED
      val g = GameMessage(objId =  me.id , msg = "GO" )
      Websocket.broadcastMessage(g)
      broadcast=false
    }
    me.save()

    translation.set(direction)
    translation.scl(Gdx.graphics.getDeltaTime() * speed)

    me._translate(translation)

    if (me.position.y <= minHeight) {
      direction.y = 1
      speed=speed*0.8f
      minHeight=minHeight -5f
      maxHeight=maxHeight*0.8f
      landed = landed - 1
    }
    if ( landed <= 0 ) {
      info(s"Invader done $me")
      gamelogic.Controller.addToDead(me)
      onLand = didILandOnLand
    }
    if (me.position.y >= maxHeight) direction.y = -1

    me.collisionCheck(objects, true)
  }

  override def collision(me: shape.Basic, other: shape.Basic): Unit = {
    super.collision(me, other)
    if ( other.isInstanceOf[shape.LandedInvader]) {
      landed=1
      didILandOnLand=false
      minHeight= -20
      info("Should not land on this")

    } else if (!other.isInstanceOf[shape.Terrain] ) {
      direction.scl(-1)
    } else {
      other.asInstanceOf[shape.Terrain].lastCollisionMatrixRef.foreach { at =>
        // let Y value decide what to do if water else up we go
        lastLandedWas = at.desc
        landed=landed -1
        if (lastLandedWas == "land" ) {
          direction.y=1
          speed=speed * 0.75f
          maxHeight=maxHeight*0.75f
          info(s"hit ${lastLandedWas}, hit ${at}")
          didILandOnLand=true
        } else {
          info(s"hit WATER !!! ${at} ${lastLandedWas}")
          minHeight= -20
          landed=1
          didILandOnLand=false
        }
      }
    }
  }

}