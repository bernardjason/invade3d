package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.{Environment, Model, ModelBatch}
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.{Matrix4, Vector3}
import org.bjason.gamelogic
import org.bjason.gamelogic.{Controller, GameInformation, GameSetup, PlayerMovement}
import org.bjason.gamelogic.basic.{move, shape}
import org.bjason.gamelogic.basic.move.Movement
import org.bjason.socket.{JsonObject, State}

case class PlayerSprite(val startPosition: Vector3 = new Vector3, val radius: Float = 8f, var movement: Movement, override val id: String = gamelogic.basic.shape.Basic.getId) extends Basic {

  lazy val genModel = gamelogic.Common.assets.get("data/hero.g3db", classOf[Model])

  val rollbackScale = -2f

  lazy val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.1f, 0.65f, 0.4f))

  var thrust = false
  var startThrust = true

  val MAX_PARTICLES = 20

  val particles = List.fill(MAX_PARTICLES)(new ThrustParticle(startPosition = startPosition))

  val localPlayer =  id.startsWith(GameSetup.playerPrefix.toString)

  override def move(objects: List[Basic]) = {
    super.move(objects)
    movement.move(objects, this)
    if ( localPlayer ) {
     if ( PlayerMovement.speed > 0  ) {
       goThrust
       jsonObject.get.other ="THRUST"
     } else {
       noThrust
       jsonObject.get.other =null
     }
    } else {
      if ( jsonObject.get.other != null ) {
        goThrust
      } else {
        noThrust
      }
    }
  }

  private def noThrust = {
    if (thrust) {
      for (p <- particles) p.visible = false
    }
    thrust = false
    startThrust = true
  }

  private def goThrust = {
    thrust = true
    val start = position.cpy
    if (startThrust) {
      for (p <- particles) {
        resetParticle(p)
        p.visible = true
      }

    }
    startThrust = false
  }

  private def resetParticle(p: ThrustParticle) = {
    p._getTransform().set(instance.transform)
    p._getTransform().rotate(0, 1, 0, 180)
    p._getTransform().translate(0, -4, 0)
  }

  val none = None

  override def collision(other: Basic) {
    movement.collision(this, other)
  }

  override def _render(modelBatch: ModelBatch, environment: Environment, cam: Camera): Unit = {
    super._render(modelBatch, environment, cam)

    if (thrust) {
      particles.map { p =>
        p.move
        p._render(modelBatch, environment, cam)
        if ( p.position.dst(position) > 12 ) {
          resetParticle(p)

        }
      }
    }
  }

  def dispose() {
    genModel.dispose()
  }

  override lazy val jsonObject = Some(new JsonObject(this.getClass.getSimpleName, id, gamelogic.Common.CHANGED, Some(State.ALIVE), instance = instance.transform))

}

case class ThrustParticle(override val startPosition: Vector3 = new Vector3, val startSize: Int = 1) extends
  shape.Cuboid(textureName = "data/explosion.jpg", startPosition = startPosition, dimensions = new Vector3(startSize, startSize, startSize), radius = 138f, movement = null) with Movement {

  movement = this
  val direction = new Vector3(Math.random().toFloat - 0.5f, 0, Math.random().toFloat - 0.5f)

  var speed = 3 + (Math.random() * 1000).toFloat % 3
  var ttl = 1f
  var visible = false
  override lazy val shape = new CollideShape {
    val radius = 0f

    override def intersects(transform: Matrix4, ray: Ray): Float = {
      Float.MaxValue
    }

    def isVisible(transform: Matrix4, cam: Camera): Boolean = {
      visible
    }
  }

  def move = {
    instance.transform.translate(direction)
    instance.transform.translate(0,1,0)
    instance.transform.getTranslation(position)
  }

  override def move(objects: List[Basic]): Unit = {
  }
}
