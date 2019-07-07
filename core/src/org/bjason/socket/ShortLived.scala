package org.bjason.socket

import org.bjason.gamelogic.Common

case class ShortLived(value:Any,liveFor:Long) {

  private val ttl = Common.currentTimeToLive + liveFor
  private val stillOk = Some(this)
  def shouldIRemove:Option[ShortLived] = {
    if ( Common.currentTimeToLive > ttl ) {
      println("EXPIRE NOW "+this)
      return None
    }
    else return stillOk
  }
}
