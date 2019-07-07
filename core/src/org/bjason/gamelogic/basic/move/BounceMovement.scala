package org.bjason.gamelogic.basic.move

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.shape
case class BounceMovement(var speed: Float = 65f) extends Movement {

  private val translation = new Vector3
  var objectToControl: shape.Basic = null

  val direction = new Vector3(0, -1, 0)
  val START_AGAIN=3
  var waterSplashes = START_AGAIN
  var maxHeight=100f
  val MIN_HEIGHT=5f
  var minHeight=MIN_HEIGHT

  override def move(objects: List[shape.Basic], me: shape.Basic) {

    me.save()

    translation.set(direction)
    translation.scl(Gdx.graphics.getDeltaTime() * speed)

    me._translate(translation)

    if (me.position.y <= minHeight) {
      randomDirection(me)
      direction.y = 1
      speed=speed*0.8f
      minHeight=minHeight -5f
      maxHeight=maxHeight*0.5f
      waterSplashes = waterSplashes - 1
      if ( waterSplashes <= 0 ) {
        info(s"Sink ball $me")
        gamelogic.Controller.addToDead(me)
      }
    }
    if (me.position.y >= maxHeight) direction.y = -1

    me.collisionCheck(objects, true)
  }

  private def randomDirection(me: shape.Basic) = {
    if (direction.x == 0 && direction.z == 0) {
      val posNeg = if (Math.random() * 1000 % 2 == 0) 1 else -1
      direction.x = Math.random().toFloat * posNeg
      direction.z = Math.random().toFloat * posNeg
    }
  }

  override def collision(me: shape.Basic, other: shape.Basic): Unit = {
    super.collision(me, other)
    randomDirection(me)
    if (!other.isInstanceOf[shape.Terrain]) {
      direction.scl(-1)
    } else {
      other.asInstanceOf[shape.Terrain].lastCollisionMatrixRef.foreach { at =>
        // let Y value decide what to do if water else up we go
        if (at.desc != "water") {
          minHeight=MIN_HEIGHT
          direction.y=1
          waterSplashes=START_AGAIN
          info(s"Did not hit water, hit ${at}")
        } else {
          info(s"Ball hit WATER !!! ${at}")
        }
      }
    }
  }

}