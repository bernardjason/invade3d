package org.bjason.socket

import com.badlogic.gdx.math.{Matrix4, Vector3}
import com.badlogic.gdx.utils.{Json, JsonValue}

case class GameMessage(
                       var id: String = null,
                       var msg: String = null,
                       var objId:String = null,
                       var objState:String = null,
                       var objMatrix4:Matrix4 = null
                     )
  extends Json.Serializable {

  def this() {
    this(null, null)
  }

  def write(json: Json): Unit = {
    json.writeValue("id", id)
    json.writeValue("msg", msg)
    json.writeValue("objId", objId)
    json.writeValue("objState", objState)
    json.writeValue("objMatrix4", objMatrix4)

  }

  def read(json: Json, jsonMap: JsonValue) = {
    id = jsonMap.get("id").asString()
    msg = jsonMap.get("msg").asString()
    objId = jsonMap.get("objId").asString()
    objState = jsonMap.get("objState").asString()
    val objMArray = jsonMap.get("objMatrix4").get("val")
    if ( objMArray != null ) objMatrix4 = new Matrix4(objMArray.asFloatArray())
  }
}

trait MessageReceiver {

  def receiveMessage(message:GameMessage): Unit = {

  }
}
