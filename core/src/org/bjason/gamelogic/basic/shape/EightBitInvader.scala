package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.move
import org.bjason.gamelogic.basic.move.{InvaderMovement, Movement}
import org.bjason.socket.{GameMessage, JsonObject, State, Websocket}

class EightBitInvader(val startPosition: Vector3 = new Vector3, val radius: Float = 16f, override val id: String = gamelogic.basic.shape.Basic.getId) extends Basic {

  val invaderMovement = new InvaderMovement()
  override var movement: Movement = invaderMovement
  lazy val genModel = gamelogic.Common.assets.get("data/8_bit_space_ivader.g3db", classOf[Model])

  val rollbackScale = -2f

  lazy val shape: CollideShape = BulletCollideBox(radius, boundingBox, basicObj = this, fudge = new Vector3(0.1f, 0.65f, 0.4f))

  override lazy val jsonObject = Some(new JsonObject(this.getClass.getSimpleName, id, gamelogic.Common.UNCHANGED, Some(State.ALIVE), instance = instance.transform))

  var moving = false

  override def move(objects: List[Basic]) = {
    super.move(objects)
    if (moving) movement.move(objects, this)
  }

  val none = None

  override def collision(other: Basic) {
    movement.collision(this, other)
  }

  override def receiveMessage(message: GameMessage): Unit = {
    super.receiveMessage(message)
    if (message.msg == "GO") {
      moving = true
      debug("MESSAGE RECEIVED !!!!!!!!!!!!!!!!!", moving, message)
    } else {
      info("WHAT MESSAGE RECEIVED !!!!!!!!!!!!!!!!!", message)
    }
  }


  def dispose() {
    genModel.dispose()
  }


  override def onDead: Option[Basic] = {

    if (invaderMovement.onLand == true) {

      val landedId = s"${id}-L"
      var found = false
      for (j <- Websocket.jsonobjects) {
        if (j.id == landedId) found = true
      }

      if (!found) {

        val where = position.cpy
        where.y = where.y - 30
        val landed = new LandedInvader(startPosition = where, dimensions = new Vector3(40, 20, 40), radius = 40, id = landedId)
        //println("BERNARD 8Bit adding LandedInvader " + landed.id + "  " + landed.jsonObject.get.id)
        landed.init
        landed.reset
        Websocket.jsonobjects += landed.jsonObject.get
        Websocket.broadcastMessage(GameMessage(msg = "Explosion", objId = gamelogic.GameSetup.playerPrefix.toString, objMatrix4 = landed.instance.transform))
        gamelogic.Explosion(position)
        return Some(landed)
      }
    }
    None
  }


}
