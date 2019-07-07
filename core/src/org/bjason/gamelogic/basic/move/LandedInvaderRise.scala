package org.bjason.gamelogic.basic.move

import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.basic.shape

object LandedInvaderRise extends Movement {

  val translation = new Vector3(0,1,0)
  override def move(objects:List[shape.Basic], me:shape.Basic) {
    if ( me.position.y < 20 ) {
      me._translate(translation)
      me.jsonObject.get.changed = gamelogic.Common.CHANGED
    }
  }
}
