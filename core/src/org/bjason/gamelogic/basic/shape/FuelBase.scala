package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.{Common, Controller, GameInformation, MinionInvader}
import org.bjason.gamelogic.basic.move.{LandedInvaderRise, NoMovement}
import org.bjason.socket.{GameMessage, JsonObject, State, Websocket}

class FuelBase(textureName: String = "data/fuelbase.png", startPosition: Vector3 = new Vector3, dimensions: Vector3 = new Vector3(8, 5, 8),
               radius: Float = 10f , override val id:String = Basic.getId)
  extends Cuboid(textureName = textureName, startPosition = startPosition, dimensions = dimensions, radius = radius,
    movement = NoMovement) {

  //override val id: String = s"C_${startPosition.x.toInt}_${startPosition.z.toInt}" //Basic.getId)

  startPosition.y = -dimensions.y + 5

  var beenHit = 1

  override lazy val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.5f, 0.5f, 0.5f))

  override lazy val jsonObject = Some(new JsonObject(this.getClass.getSimpleName, id, gamelogic.Common.UNCHANGED, Some(State.ALIVE), instance = instance.transform))

  override def collision(other: Basic) {
    println("BERNARD1 ",other)
    other match {
      case _:AlienMissileShape|_:MinionInvader =>
        println("************************* BERNARD1 ",other)
        beenHit = beenHit - 1
        if (beenHit <= 0) {
          Controller.addToDead(this)
          Websocket.broadcastMessage(GameMessage(msg = "Explosion", objId = gamelogic.GameSetup.playerPrefix.toString, objMatrix4 = instance.transform))
          gamelogic.Explosion(position)
        }
      case _ =>
    }
  }

  override lazy val texture = Common.assets.get(textureName, classOf[Texture])


  override def getTextureRegions(texture: Texture, textureBlockWidth: Int, textureBlockHeight: Int) = {
    val wide = 128
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

  override def onDead: Option[Basic] = {
    super.onDead
  }
}
