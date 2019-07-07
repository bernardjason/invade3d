package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.{Pixmap, Texture}
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic.Common
import org.bjason.gamelogic.basic.move.NoMovement

class AlienGround(textureName: String = "data/surface.png", startPosition: Vector3 = new Vector3, dimensions: Vector3 = new Vector3(128,1, 128),
                  override val id:String = Basic.getId)
  extends Cuboid(textureName = textureName, startPosition = startPosition, dimensions = dimensions,
    movement = NoMovement) {
    override val radius = (dimensions.x + dimensions.z) /1.5f

    override lazy val texture = Common.assets.get(textureName,classOf[Texture])


    override def getTextureRegions(texture: Texture, textureBlockWidth: Int, textureBlockHeight: Int) = {
        val wide=128
        val textureregion = Array(
            new TextureRegion(texture, 0, 0, wide, wide),
            new TextureRegion(texture, 0, 0, wide, wide),
            new TextureRegion(texture, 0, 0, wide, wide),
            new TextureRegion(texture, 0, 0, wide, wide),
            new TextureRegion(texture, 0, 0, wide, wide),
            new TextureRegion(texture, 0, 0, wide, wide)
        )
        textureregion
    }
}


