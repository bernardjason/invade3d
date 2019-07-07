package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.Controller
import org.bjason.gamelogic.basic.move.{LandedInvaderRise, NoMovement}
import org.bjason.socket.{JsonObject, State}

class FuelBase(textureName: String = "data/aliencube.jpg", startPosition: Vector3 = new Vector3, dimensions: Vector3 = new Vector3(40, 20, 40),
               radius: Float = 40f, override val id:String = Basic.getId)
  extends Cuboid(textureName = textureName, startPosition = startPosition, dimensions = dimensions, radius = radius,
    movement = NoMovement) {

    startPosition.y = -18

    var beenHit=1

    override lazy val jsonObject = Some(new JsonObject(this.getClass.getSimpleName, id, gamelogic.Common.CHANGED, Some(State.ALIVE), instance = instance.transform))

    override def collision(other:Basic) {
        if ( other.isInstanceOf[AlienMissileShape]) {
           beenHit=beenHit-1
            if ( beenHit <= 0 ) {
              Controller.addToDead(this)
            }
        }
    }
}
