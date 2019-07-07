package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.{Intersector, Matrix4, Vector3}
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.physics.bullet.collision.{btCollisionObject, btCollisionShape}
import org.bjason.gamelogic.basic.shape



trait CollideShape {

  var bulletObject: btCollisionObject = null
  var bulletShape: btCollisionShape = null

  val bullet = false
  def intersects(transform: Matrix4, ray: Ray): Float

  val position = new Vector3();
  val center = new Vector3();
  val radius: Float

  def isVisible(transform: Matrix4, cam: Camera): Boolean

  def defaultIsVisible(transform: Matrix4, cam: Camera): Boolean = {
    cam.frustum.sphereInFrustum(transform.getTranslation(position).add(center), radius);
  }
  def bulletCheckCollision(obj0: btCollisionObject, obj1: btCollisionObject): Boolean = {
    return false
  }
  def removeBulletWorld = {

  }

  def setBulletWorldTransform(basic: Basic) {

  }
  def addBulletWorld(basic: Basic, dontadd: Boolean) {

  }
  def dispose = {

  }
}

case class CollideSphere(radius:Float) extends CollideShape {
  val intersection = new Vector3
  override def intersects(transform:Matrix4 , ray:Ray ) :Float = {
        transform.getTranslation(position).add(center);
        
        if ( Intersector.intersectRaySphere(ray, position, radius, intersection) == true ) {
          val len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
          if ( len < 0 ) return Float.MaxValue
          return len
        } 
        Float.MaxValue
    }
  def isVisible(transform: Matrix4, cam: Camera): Boolean = defaultIsVisible(transform,cam)
}