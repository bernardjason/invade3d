package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.{Material, Model}
import com.badlogic.gdx.graphics.{Camera, GL20, Texture, VertexAttributes}
import com.badlogic.gdx.math.collision.Ray
import com.badlogic.gdx.math.{Intersector, Matrix4, Vector3}
import org.bjason.gamelogic
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.move.{Movement, NoMovement}

import scala.Array.ofDim

abstract class BaseTerrain(override val position: Vector3, textureName: String) extends Basic {

  val originalStartPosition = position.cpy()
  val startPosition: Vector3 = new Vector3
  val radius: Float = 0f
  var movement: Movement = NoMovement
  val terrainGroundZero: Int
  val terrainSize: Int
  val blockSize: Int
  var lastCollisionMatrixRef:Option[AtPosition] = None

  case class AtPosition(var height: Float = (Math.random() * 10000 % 100 ).toFloat, var terrainTextureOffset: Int = 0, var desc: String = "def", objs: List[String] = List())

  lazy val matrix = ofDim[AtPosition](terrainSize / blockSize, terrainSize / blockSize)

  Array.fill(terrainGroundZero)(matrix)
  lazy val texture = gamelogic.Common.assets.get(textureName, classOf[Texture])
  lazy val genModel = makeGround(texture)

  override def init {
    startPosition.set(-terrainSize / 2, 0, -terrainSize / 2)
    startPosition.add(position)
  }

  override def move(objects: List[Basic]) = {
    movement.move(objects, this)
  }

  override def collision(other: Basic) {
    movement.collision(this,other)
  }

  def dispose() {
    genModel.dispose();
  }

  val shape = new CollideShape {
    val radius = 0f

    override def intersects(transform: Matrix4, ray: Ray): Float = {
      Float.MaxValue
    }
    def isVisible(transform: Matrix4, cam: Camera): Boolean = defaultIsVisible(transform,cam)
  }

  val rayDown = new Vector3(0, -1, 0)
  val intersection = new Vector3
  val adjPos = new Vector3

  def hitMe(what: Vector3, radius: Float): Boolean = {

    val offx = if ( what.x < 0 ) Terrain.terrainSize else -Terrain.terrainSize
    val offz = if ( what.z < 0 ) Terrain.terrainSize else -Terrain.terrainSize

    val xx = what.x - position.x
    val zz = what.z - position.z
    adjPos.x = xx
    adjPos.y = what.y
    adjPos.z = zz

    var bx = ( (xx-2) / blockSize).toInt
    var bz = ( (zz-2) / blockSize).toInt
    if( bx < 0 ) bx=0
    if( bz < 0 ) bz=0
    if ( bx >= Terrain.cellMax ) bx= Terrain.cellMax-1
    if ( bz >= Terrain.cellMax ) bz= Terrain.cellMax-1

    if (bx <= Terrain.cellMax && bz <= Terrain.cellMax && bx >= 0 && bz >= 0) {

      val offx=if ( bx == Terrain.cellMax-1 ) 0 else 1
      val offz=if ( bz == Terrain.cellMax-1 ) 0 else 1
      val ray = new Ray(adjPos, rayDown)
      val points = Array((bx) * blockSize, matrix(bz)(bx).height, (bz) * blockSize,
        (bx + 1) * blockSize, matrix(bz + offz)(bx + offx).height, (bz + 1) * blockSize,
        (bx + 1) * blockSize, matrix(bz)(bx + offx).height, (bz) * blockSize,

        (bx) * blockSize, matrix(bz + offz)(bx).height, (bz + 1) * blockSize,
        (bx + 1) * blockSize, matrix(bz + offz)(bx + offx).height, (bz + 1) * blockSize,
        (bx) * blockSize, matrix(bz)(bx).height, (bz) * blockSize)


      Intersector.intersectRayTriangles(ray, points, intersection)
      val dst = adjPos.dst(intersection)

      if (dst < radius / 2) {
        debug("!!!hit " + matrix(bz)(bx))
        val what = matrix(bz)(bx)
        lastCollisionMatrixRef = Some( what )
        return true
      }

    } else {
      info(  s"Terrain.cellMax=${Terrain.cellMax} what=${what}")
      info(  s"what=${what.x.toInt},${what.z.toInt}  xx,zz= ${xx.toInt},${zz.toInt} bx=${bx},bz=${bz}  ")
      //info(" more info", s" ${Common.terrain}, ${xx.toInt}, ${zz.toInt}, ${originalStartPosition}, ${position}")
      //System.exit(0)
    }
    lastCollisionMatrixRef = None
    false
  }



  def makeGround(texture: Texture): Model = {
    val attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

    val textureBlockHeight = texture.getHeight
    //val textureBlockWidth = texture.getWidth / textureBlockHeight
    val textureBlockWidth = textureBlockHeight

    modelBuilder.begin();

    val mesh = modelBuilder.part("box", GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(texture)));

    val textureregion = List.tabulate(texture.getWidth /textureBlockHeight.toInt)( off =>
      new TextureRegion(texture, textureBlockWidth * off, 0, textureBlockWidth, textureBlockHeight)
    )

    setupHeightMatrix

    val c0 = new Vector3
    val c1 = new Vector3
    val c2 = new Vector3
    val c3 = new Vector3
    val normal = new Vector3(1, 0, 0)

    for (zz <- 0 until (terrainSize) / blockSize ) {
      for (xx <- 0 until (terrainSize) / blockSize) {
        val x = xx * blockSize
        val z = zz * blockSize

        val nextx=if (xx == terrainSize/blockSize-1)  0 else 1
        val nextz=if (zz == terrainSize/blockSize-1)  0 else 1
        c0.set(x, matrix(zz)(xx).height, z)
        c1.set(x + blockSize, matrix(zz)(xx + nextx).height, z)
        c2.set(x, matrix(zz + nextz)(xx).height, z + blockSize)
        c3.set(x + blockSize, matrix(zz + nextz)(xx + nextx).height, z + blockSize)
        val c = matrix(zz)(xx).terrainTextureOffset
        mesh.setUVRange(textureregion(c))
        mesh.rect(c0, c2, c3, c1, normal)

      }
    }

    modelBuilder.end();
  }

  def setupHeightMatrix() {

  }
}