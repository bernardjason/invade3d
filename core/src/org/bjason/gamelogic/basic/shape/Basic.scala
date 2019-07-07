package org.bjason.gamelogic.basic.shape

import java.io.{File, PrintWriter}

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.graphics.g3d.{Environment, Model, ModelBatch, ModelInstance}
import com.badlogic.gdx.math.collision.{BoundingBox, Ray}
import com.badlogic.gdx.math.{Matrix4, Quaternion, Vector3}
import org.bjason.gamelogic
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.move.Movement
import org.bjason.socket.{JsonObject, MessageReceiver}

abstract class Basic extends MessageReceiver {
  val modelBuilder = new ModelBuilder();
  val startPosition: Vector3
  val position = new Vector3()
  val lastMove = new Vector3()
  val rollbackScale:Float
  var dead = false

  lazy val jsonObject:Option[JsonObject] = None

  def _getTransform() = {
    instance.transform
  }
  def _render(modelBatch: ModelBatch,environment:Environment,cam:Camera) = {
    if ( display ) {
      if (shape.isVisible(instance.transform, cam)){
        modelBatch.render(instance, environment)
      }
    };
  }
  def _rotate( axisX:Float, axisY:Float, axisZ:Float, degrees:Float) = {
    instance.transform.rotate(axisX,axisY,axisZ,degrees)
  }
  def _getRotation(q: Quaternion) = {
    instance.transform.getRotation(q)
  }

  def _translate(t:Vector3): Unit = {
    lastMove.set(t)
    instance.transform.translate(t)
    if (shape.bulletObject != null) shape.bulletObject.setWorldTransform(_getTransform)
    instance.transform.getTranslation(position)
  }
  def _setBulletObject = {
    if (shape.bulletObject != null) shape.bulletObject.setWorldTransform(_getTransform)
    instance.transform.getTranslation(position)
  }
  private val _t = new Vector3
  def _translate(x:Float,y:Float,z:Float): Unit = {
    _t.set(x,y,z)
    _translate(_t)
  }
  def _trn(t:Vector3): Unit = {
    lastMove.set(t)
    instance.transform.trn(t)
    if (shape.bulletObject != null) shape.bulletObject.setWorldTransform(_getTransform)
    instance.transform.getTranslation(position)
  }

  var movement: Movement
  val radius: Float
  val shape: CollideShape
  val genModel: Model

  lazy val instance = new ModelInstance(genModel);
  val raydirection = new Vector3(0, 0, 0)
  val oldPosition = new Matrix4
  val tmpPositionHolder = new Vector3
  val originalMatrix4 = new Matrix4

  private var _boundindBox:BoundingBox =null; // new BoundingBox
  def boundingBox = {
    _boundindBox = new BoundingBox()
    instance.calculateBoundingBox(_boundindBox)
  }

  private var flashCounter = 0

  val id = Basic.getId

  override def hashCode: Int = {
    id.hashCode
  }
  override def equals(that: Any): Boolean =
    that match {
      case that: Basic => that.id == this.id
      case _           => false
    }

  def flash = {
    flashCounter = 60
  }

  def display: Boolean = {
    if (flashCounter > 0) {
      flashCounter = flashCounter - 1
      if (flashCounter % 3 == 1) return false
    }
    true
  }
  def init {
    originalMatrix4.set(instance.transform)
  }

  def reset = {
    instance.transform.set(originalMatrix4)
    instance.transform.trn(startPosition)
    instance.transform.getTranslation(position)
    shape.setBulletWorldTransform(this)

  }

  def animate = {

  }



  def move(objects: List[Basic]): Unit = {
    instance.transform.getTranslation(position)
  }

  def collision(other: Basic) {

  }

  def collisionCheck(objects: List[Basic],terrainOnly:Boolean=false) = {
    var collided = false

    if ( ! terrainOnly ) {
      //val ray = new Ray(instance.transform.getTranslation(tmpPositionHolder), raydirection)
      val ray = new Ray(position, raydirection)

      for (o <- objects) {
        if (this != o) {
          val len = o.shape.intersects(o.instance.transform, ray)
          if (len != Float.MaxValue && (len < radius || len < o.radius)) {
            collision(o)
            o.collision(this)
            collided = true
          }
        }
      }
    }
    if (collided == false ) {
        gamelogic.Controller.terrains.get(Terrain.positionToKey(position.x , position.z )).map { t1 =>
          if (t1.hitMe(position, radius)) {
            collision(t1)
            debug("hit terrain")
        }
      }
    }
  }

  def save() = {
    oldPosition.set(instance.transform)
  }

  def rollback = {
    instance.transform.setToTranslation(position)
    instance.transform.set(oldPosition)
    //lastMove.scl(rollbackScale)
    //instance.transform.trn(lastMove)
    instance.transform.getTranslation(position)

    if (shape.bulletObject != null) shape.bulletObject.setWorldTransform(_getTransform)
    debug("ROLLBACK")
  }

  def onDead:Option[Basic] = {
    None
  }

  def distanceTravelled = {
    instance.transform.getTranslation(tmpPositionHolder)
    startPosition.dst(tmpPositionHolder)
  }
}

object Basic {

  def getNextPlayerIdFromFile() = {
    val filename="/src/gdx/gamelogic/player.txt"
    val f = new File(filename)
    val nextId = if ( f.exists() ) {
      val source = scala.io.Source.fromFile(filename)
      val lines = try source.mkString finally source.close()
      lines.toInt +1
    } else {
      1
    }
    new PrintWriter(filename) {
      write(s"${nextId}"); close
    }
    nextId
  }

  lazy val playerPrefix = s"${gamelogic.GameSetup.playerPrefix}_"

  var id = 0;
  def getId = {
    id = id + 1
    s"${playerPrefix}${id-1}"
  }
}