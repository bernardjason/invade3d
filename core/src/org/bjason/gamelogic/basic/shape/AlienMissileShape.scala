package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.{Environment, Material, Model, ModelBatch, ModelInstance}
import com.badlogic.gdx.graphics.{Camera, GL20, Pixmap, Texture, VertexAttributes}
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.{Common, MinionInvader, Sound, basic}
import org.bjason.gamelogic.basic.move.Movement
import org.bjason.socket.{JsonObject, State}

case class AlienMissileShape(val startPosition: Vector3 = new Vector3, val radius: Float = 8, var movement: Movement, override val id: String = basic.shape.Basic.getId) extends Basic {

  lazy val texture = Common.assets.get("data/alienmissile.jpg", classOf[Texture])
  lazy val genModel = makeRect(texture, radius / 3, radius * 0.75f)
  lazy val instance2 = new ModelInstance(genModel);
  lazy val instance3 = new ModelInstance(genModel);

  val rollbackScale = 0f

  lazy val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.7f, 1.25f, 0.7f))

  override lazy val jsonObject = Some(new JsonObject(this.getClass.getSimpleName, id, gamelogic.Common.CHANGED, Some(State.ALIVE), instance = instance.transform))

  Sound.alienMissile

  override def reset = {
    super.reset
    instance2.transform.set(originalMatrix4)
    instance2.transform.trn(startPosition)
    instance2.transform.getTranslation(position)
    offsetTail
  }

  val JIGGLE = 7
  val JIGGLE_OFF_BY = 12
  var jiggle = -JIGGLE
  var jiggleBy = 2
  var startToDisplayTailAfter = 0.25f

  def offsetTail = {
    jiggle = jiggle + jiggleBy
    if (jiggle <= -JIGGLE || jiggle >= JIGGLE) {
      jiggleBy = jiggleBy * -1
    }
    instance2.transform.setToTranslation(position)
    instance2.transform.rotate(1, 0, 0, jiggle)
    instance2.transform.translate(0, JIGGLE_OFF_BY, 0)

    instance3.transform.setToTranslation(position)
    instance3.transform.rotate(1, 0, 0, -jiggle)
    instance3.transform.translate(0, JIGGLE_OFF_BY * 2, 0)
  }

  override def move(objects: List[Basic]) = {
    movement.move(objects, this)
  }

  override def collision(other: Basic) {

    movement.collision(this, other)
    other match {
      case _:MinionInvader =>
      case _ => Sound.soptAlienMissileShape
    }
  }

  override def _render(modelBatch: ModelBatch, environment: Environment, cam: Camera): Unit = {
    if (display) {
      offsetTail
      startToDisplayTailAfter = startToDisplayTailAfter - Gdx.graphics.getDeltaTime
      if (shape.isVisible(instance.transform, cam)) {
        modelBatch.render(instance, environment)
        if (startToDisplayTailAfter <= 0) {
          modelBatch.render(instance2, environment)
          modelBatch.render(instance3, environment)
        }
      }
    }
  }

  def dispose() {
    genModel.dispose();
  }


  def makeRect(texture: Texture, size: Float = 2f, height: Float = 4f): Model = {
    val attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

    val textureBlockWidth = texture.getWidth
    val textureBlockHeight = texture.getHeight

    modelBuilder.begin();

    val mesh = modelBuilder.part("rect", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(texture)));

    val textureregion = Array(
      new TextureRegion(texture, 0, 0, textureBlockWidth, textureBlockHeight))
    mesh.setUVRange(textureregion(0));
    mesh.rect(-size, -height, -size, -size, height, -size, size, height, -size, size, -height, -size, 0, 0, -1);
    mesh.setUVRange(textureregion(0));
    mesh.rect(-size, height, size, -size, -height, size, size, -height, size, size, height, size, 0, 0, 1);
    mesh.setUVRange(textureregion(0));
    mesh.rect(-size, -height, size, -size, -height, -size, size, -height, -size, size, -height, size, 0, -1, 0);
    mesh.setUVRange(textureregion(0));
    mesh.rect(-size, height, -size, -size, height, size, size, height, size, size, height, -size, 0, 1, 0);
    mesh.setUVRange(textureregion(0));
    mesh.rect(-size, -height, size, -size, height, size, -size, height, -size, -size, -height, -size, -1, 0, 0);
    mesh.setUVRange(textureregion(0));
    mesh.rect(size, -height, -size, size, height, -size, size, height, size, size, -height, size, 1, 0, 0);

    modelBuilder.end();
  }


  def makeBox(texture: Texture, size: Float = 2f): Model = {
    val attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

    val textureBlockWidth = texture.getWidth / 6
    val textureBlockHeight = texture.getHeight

    modelBuilder.begin();

    val mesh = modelBuilder.part("box", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(texture)));

    val textureregion = Array(
      new TextureRegion(texture, textureBlockWidth * 0, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 1, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 2, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 3, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 4, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 5, 0, textureBlockWidth, textureBlockHeight))
    mesh.setUVRange(textureregion(0));
    mesh.rect(-size, -size, -size, -size, size, -size, size, size, -size, size, -size, -size, 0, 0, -1);
    mesh.setUVRange(textureregion(1));
    mesh.rect(-size, size, size, -size, -size, size, size, -size, size, size, size, size, 0, 0, 1);
    mesh.setUVRange(textureregion(2));
    mesh.rect(-size, -size, size, -size, -size, -size, size, -size, -size, size, -size, size, 0, -1, 0);
    mesh.setUVRange(textureregion(3));
    mesh.rect(-size, size, -size, -size, size, size, size, size, size, size, size, -size, 0, 1, 0);
    mesh.setUVRange(textureregion(4));
    mesh.rect(-size, -size, size, -size, size, size, -size, size, -size, -size, -size, -size, -1, 0, 0);
    mesh.setUVRange(textureregion(5));
    mesh.rect(size, -size, -size, size, size, -size, size, size, size, size, -size, size, 1, 0, 0);

    modelBuilder.end();
  }

}
