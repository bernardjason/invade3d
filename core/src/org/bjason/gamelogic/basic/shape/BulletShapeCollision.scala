package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.collision.{BoundingBox, Ray}
import com.badlogic.gdx.math.{Intersector, Matrix4, Vector3}
import com.badlogic.gdx.physics.bullet.collision.{btCylinderShape, _}
import org.bjason.gamelogic
import org.bjason.gamelogic.Log._;

object slowprint {
  var every = 0
  val tick = 100
  def doit(callback: () => Unit) = { every = every + 1; if (every % tick == 1) callback() }
}

case class NeverCollide() extends CollideShape {
  val radius = 0f
  override def intersects(transform: Matrix4, ray: Ray): Float = {
    Float.MaxValue
  }
  def isVisible(transform: Matrix4, cam: Camera): Boolean = defaultIsVisible(transform,cam)
}



abstract class BulletShape extends CollideShape {
  override val bullet = true

  val intersection = new Vector3


  override def addBulletWorld(basic: Basic, dontAdd: Boolean) {
    if (dontAdd == false) {
      if (bulletObject != null) {
        info("******* Already in world so remove old reference")
        removeBulletWorld
      }
      bulletObject = new btCollisionObject();
      bulletObject.setCollisionShape(bulletShape);
      bulletObject.setContactCallbackFilter(2)
      bulletObject.setContactCallbackFlag(2)
      bulletObject.userData = basic
      gamelogic.Controller.collisionWorld.addCollisionObject(bulletObject);
    }
  }
  override def setBulletWorldTransform(basic: Basic) {
    if (bulletObject != null) bulletObject.setWorldTransform(basic._getTransform)
  }

  override def removeBulletWorld {
    if (bulletObject != null) {
      gamelogic.Controller.collisionWorld.removeCollisionObject(bulletObject)
      bulletObject.userData = null
      gamelogic.Controller.addBulletObjectToDispose(this)
      bulletObject = null
    }
  }

  override def dispose = {
    if (bulletObject != null) { //bulletObject.userData != null ) {
      gamelogic.Controller.collisionWorld.removeCollisionObject(bulletObject)
      bulletObject.dispose()
      bulletObject = null
    }
    if (bulletShape != null) {
      bulletShape.dispose()
      bulletShape = null
    }
  }

  override def intersects(transform: Matrix4, ray: Ray): Float = {
    bulletObject.setWorldTransform(transform)
    transform.getTranslation(position).add(center);

    if (Intersector.intersectRaySphere(ray, position, radius, intersection) == true) {
      val len = ray.direction.dot(position.x - ray.origin.x, position.y - ray.origin.y, position.z - ray.origin.z);
      if (len < 0) return Float.MaxValue
      return len
    }
    Float.MaxValue
  }

}

case class BulletCollideSphere(radius: Float, basicObj: Basic, dontAdd: Boolean = false) extends BulletShape {
  bulletShape = new btSphereShape(radius);
  this.addBulletWorld(basicObj, dontAdd)
  def isVisible(transform: Matrix4, cam: Camera): Boolean = defaultIsVisible(transform,cam)
}

case class BulletCollideBox(radius: Float, boundingBox: BoundingBox, basicObj: Basic, dontAdd: Boolean = false, fudge: Vector3 = new Vector3(1, 1, 1), iknowbest: Vector3 = null) extends BulletShape {
  val dimensions = new Vector3

  if (iknowbest != null) dimensions.set(iknowbest)
  else boundingBox.getDimensions(dimensions)

  dimensions.scl(fudge)
  bulletShape = new btBoxShape(dimensions)

  this.addBulletWorld(basicObj, dontAdd)
  def isVisible(transform: Matrix4, cam: Camera): Boolean = defaultIsVisible(transform,cam)

}

class BulletCollideCylinder(val radius: Float, basicObj: Basic, dontAdd: Boolean = false,  dimensions: Vector3 = null) extends BulletShape {

  bulletShape = new btCylinderShape(dimensions)

  this.addBulletWorld(basicObj, dontAdd)

  def isVisible(transform: Matrix4, cam: Camera): Boolean = defaultIsVisible(transform,cam)
}

case class BulletCollideOther(radius: Float, boundingBox: BoundingBox, basicObj: Basic, dontAdd: Boolean = false, fudge: Vector3 = new Vector3(1, 1, 1), iknowbest: Vector3 = null) extends BulletShape {
  val dimensions = new Vector3

  if (iknowbest != null) dimensions.set(iknowbest)
  else boundingBox.getDimensions(dimensions)

  dimensions.scl(fudge)
  bulletShape = new btCylinderShape(dimensions)

  this.addBulletWorld(basicObj, dontAdd)
  def isVisible(transform: Matrix4, cam: Camera): Boolean = defaultIsVisible(transform,cam)

}

class MyContactListener extends ContactListener {
  val here = new Vector3

  override def onContactStarted(obj0: btCollisionObject, match0: Boolean, obj1: btCollisionObject, match1: Boolean) {
    info("*********** onContactStarted CONTACT *********** 0=", obj0.userData, obj0, "  1=", obj1.userData,obj1, "  match1=" + match1)
    if (obj0.userData != null && obj1.userData != null) {
      val me = obj0.userData.asInstanceOf[Basic]
      val other = obj1.userData.asInstanceOf[Basic]
      info("*********** CONTACT *********** 0=", me.id + "  1=" + other.id)
      if (me.id == other.id) {
        info("*********** DANGER CONTACT *********** 0=", me.id + "  " + other.id)
        info("*********** DANGER CONTACT *********** 0=", obj0, "  1=", obj1)
        System.exit(0)
      } else {
        obj0.getWorldTransform.getTranslation(here)
        debug("OBJ0 = " + here)
        obj1.getWorldTransform.getTranslation(here)
        debug("OBJ1 = " + here)

        me.collision(other)
        other.collision(me)
      }
    }
  }
}

