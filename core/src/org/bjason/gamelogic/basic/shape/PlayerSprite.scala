package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.basic.move
import org.bjason.gamelogic.basic.move.Movement
import org.bjason.socket.{JsonObject, State}

case class PlayerSprite(val startPosition: Vector3 = new Vector3, val radius: Float = 8f, var movement: Movement, override val id: String = gamelogic.basic.shape.Basic.getId) extends Basic {

  lazy val genModel = gamelogic.Common.assets.get("data/hero.g3db", classOf[Model])

  val rollbackScale = -2f

  lazy val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.1f, 0.65f, 0.4f))

  override def move(objects: List[Basic]) = {
    super.move(objects)
    movement.move(objects, this)
  }

  val none = None

  override def collision(other: Basic) {
    movement.collision(this, other)
  }


  def dispose() {
    genModel.dispose()
  }

  override lazy val jsonObject = Some(new JsonObject(this.getClass.getSimpleName, id, gamelogic.Common.CHANGED, Some(State.ALIVE), instance = instance.transform))

}
