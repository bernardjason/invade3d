package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes.Usage
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.attributes.{ColorAttribute, FloatAttribute, TextureAttribute}
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic.basic
import org.bjason.gamelogic.basic.move
import org.bjason.gamelogic.basic.move.Movement

case class Ball(val startPosition: Vector3 = new Vector3, val radius: Float = 4f, var movement: Movement, override val id:String = basic.shape.Basic.getId) extends Basic {

  val texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
  val rollbackScale= -8f

  val material = new Material(TextureAttribute.createDiffuse(texture), ColorAttribute.createSpecular(1, 1, 1, 1),
    FloatAttribute.createShininess(8f));

  val attributes = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

  lazy val genModel = modelBuilder.createSphere(radius, radius, radius, 8, 8, material, attributes)

  val shape: CollideShape = new BulletCollideSphere(radius/2,this)

  override def move(objects: List[Basic]) = {
    movement.move(objects, this)
  }

  override def collision(other: Basic) {
    movement.collision(this,other)
  }

  def dispose() {
    genModel.dispose();
  }

}