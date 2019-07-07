package org.bjason.gamelogic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.{Matrix4, Vector3}
import org.bjason.gamelogic.basic.move.Movement
import org.bjason.gamelogic.basic.shape.CollideShape
import org.bjason.gamelogic.basic.{move, shape}

case class Explosion(startPosition: Vector3) extends move.Movement {


  override def move(objects: List[shape.Basic], me: shape.Basic) {

    val p =me.asInstanceOf[Particle]
    val translation = p.direction.cpy.scl(p.speed)
    p.speed = p.speed * 0.98f

    p.instance.transform.rotate(Math.random().toFloat,Math.random().toFloat,Math.random().toFloat,10)
    p._trn(translation)
    p.ttl = p.ttl - Gdx.graphics.getDeltaTime
    if (p.ttl < 0) {
      Controller.addToDead(p)
    }
    p.instance.transform.scl(0.95f)

  }

  override def collision(me: shape.Basic, other:shape.Basic) {
  }

  val startSize=4f
  val max = 30

  case class Particle(override val startPosition: Vector3 = new Vector3, val move: Movement) extends
    shape.Cuboid(textureName = "data/explosion.jpg", startPosition = startPosition, dimensions = new Vector3(startSize, startSize, startSize), radius = 138f, movement = move) {

    val direction = new Vector3(Math.random().toFloat-0.5f, Math.random().toFloat-0.5f, Math.random().toFloat-0.5f)

    var speed = 3+(Math.random()*1000).toFloat % 3
    var ttl = 1f
    override lazy val shape = new CollideShape {
      val radius = 0f

      override def intersects(transform: Matrix4, ray: Ray): Float = {
        Float.MaxValue
      }
      def isVisible(transform: Matrix4, cam: Camera): Boolean = true
    }
  }

  for (particles <- 0 to max) {
    val p = new Particle(startPosition = startPosition, move = this) {
    }
    Controller.addNewBasic(p)
  }
}
