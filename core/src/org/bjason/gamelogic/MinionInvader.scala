package org.bjason.gamelogic

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.basic.move.{Movement, NoMovement}
import org.bjason.gamelogic.basic.shape.{AlienMissileShape, Basic, BulletCollideBox, CollideShape}

class MinionInvader(val minionStatus:MinionStatusValue, val startPosition:Vector3, val radius: Float = 16f) extends Basic {

  override var movement: Movement = NoMovement
  lazy val genModel = gamelogic.Common.assets.get("data/8_bit_space_ivader.g3db", classOf[Model])

  val rollbackScale = -2f
  var onScreen=true

  //lazy val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.1f, 0.65f, 0.4f))
  lazy val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.6f, 0.85f, 0.8f))

  override def move(objects: List[Basic]) = {
    super.move(objects)
  }

  val none = None

  override def collision(other: Basic) {
    movement.collision(this, other)
    if ( ! other.isInstanceOf[AlienMissileShape]) {
      minionStatus.dead
    }
  }


  def dispose() {
    genModel.dispose()
  }

  override def onDead = {
    onScreen=false
    Some(this)
  }


}
