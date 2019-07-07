package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.attributes.{BlendingAttribute, TextureAttribute}
import com.badlogic.gdx.graphics.g3d.{Material, Model}
import com.badlogic.gdx.graphics.{GL20, Pixmap, Texture, VertexAttributes}
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.basic
import org.bjason.gamelogic.basic.move
import org.bjason.gamelogic.basic.move.Movement

class Cuboid(val textureName:String, override val startPosition:Vector3 = new Vector3, val dimensions:Vector3,
             val radius:Float=4f, var movement:Movement, override val id:String = basic.shape.Basic.getId) extends Basic {
   val height=radius
  val width=radius
  val depth=radius
  val rollbackScale= 0f

  lazy val texture = createBlockTexture(textureName)
  lazy val genModel = makeCuboid(texture,dimensions.x,dimensions.y,dimensions.z) 
  lazy val shape: CollideShape = BulletCollideBox(radius,boundingBox,basicObj=this,fudge = new Vector3(0.5f, 0.5f, 0.5f))

  override def move(objects:List[Basic])  = {
    movement.move(objects, this)  
  }

  override def collision(other: Basic) {
    movement.collision(this,other)
  }
  

  def makeCuboid(texture:Texture, x:Float=2f,y:Float,z:Float): Model = {
    val attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates
    
    val textureBlockWidth = texture.getWidth/6
    val textureBlockHeight = texture.getHeight
    
    modelBuilder.begin()
    
    val mesh = modelBuilder.part("box", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(texture)))

    val textureregion = getTextureRegions(texture, textureBlockWidth, textureBlockHeight)

    mesh.setUVRange(textureregion(0))
    mesh.rect(-x,-y,-z,   -x,y,-z,  x,y,-z, x,-y,-z, 0,0,-1)
    mesh.setUVRange(textureregion(1))
    mesh.rect(-x,y,z, -x,-y,z,  x,-y,z, x,y,z, 0,0,1)
    mesh.setUVRange(textureregion(2))
    mesh.rect(-x,-y,z, -x,-y,-z,  x,-y,-z, x,-y,z, 0,-1,0)
    mesh.setUVRange(textureregion(3))
    mesh.rect(-x,y,-z, -x,y,z,  x,y,z, x,y,-z, 0,1,0)
    mesh.setUVRange(textureregion(4))
    mesh.rect(-x,-y,z, -x,y,z,  -x,y,-z, -x,-y,-z, -1,0,0)
    mesh.setUVRange(textureregion(5))
    mesh.rect(x,-y,-z, x,y,-z,  x,y,z, x,-y,z, 1,0,0)
    
    val model = modelBuilder.end()
    model.materials.get(0).set(new BlendingAttribute(true,1))

    model
  }


  def getTextureRegions(texture: Texture, textureBlockWidth: Int, textureBlockHeight: Int) = {
    val textureregion = Array(
      new TextureRegion(texture, textureBlockWidth * 0, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 1, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 2, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 3, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 4, 0, textureBlockWidth, textureBlockHeight),
      new TextureRegion(texture, textureBlockWidth * 5, 0, textureBlockWidth, textureBlockHeight)
    )
    textureregion
  }

  /** creates a texture for use in this class that has had the list of regions rotated to match expectation.
 *  imagine a jpg with a list of 6 areas, 1 2 3 4 5 6.   Method here in case some manipulation wanted
 *
 * @param textureName which is expected to be a loaded asset
 * @return
 */
def createBlockTexture(textureName: String) = {
    val pixmap = gamelogic.Common.assets.get(textureName,classOf[Pixmap])
    
    val textureBlockWidth=pixmap.getWidth/6
    val textureBlockHeight=pixmap.getHeight
        
    val texture = new Texture(pixmap.getWidth,pixmap.getHeight,pixmap.getFormat)

    for(i <- 0 to 5 ) {
      val pixmapX=i*textureBlockWidth
         
      val oneBitOfTexture = new Pixmap(textureBlockWidth, textureBlockHeight, pixmap.getFormat)
      oneBitOfTexture.drawPixmap(pixmap, 0,0,pixmapX, 0,textureBlockWidth,textureBlockHeight)     
      
      texture.draw(oneBitOfTexture, pixmapX, 0)
      oneBitOfTexture.dispose()
    } 
    texture
  }
}
