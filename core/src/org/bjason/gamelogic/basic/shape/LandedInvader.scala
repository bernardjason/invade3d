package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.basic.move.LandedInvaderRise
import org.bjason.socket.{JsonObject, State}

class LandedInvader(textureName: String = "data/aliencube.jpg", startPosition: Vector3 = new Vector3, dimensions: Vector3 = new Vector3(40, 20, 40),
                    radius: Float = 40f,override val id:String = Basic.getId)
  extends Cuboid(textureName = textureName, startPosition = startPosition, dimensions = dimensions, radius = radius,
    movement = LandedInvaderRise) {

    override lazy val jsonObject = Some(new JsonObject(this.getClass.getSimpleName, id, gamelogic.Common.CHANGED, Some(State.ALIVE), instance = instance.transform))

}
