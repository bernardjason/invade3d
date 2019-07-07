package org.bjason.gamelogic.basic.move

import org.bjason.gamelogic.basic.shape.Basic
import org.bjason.gamelogic.Log._

trait Movement {
  def move(objects: List[Basic], me: Basic) = {

  }

  def collision(me: Basic,other:Basic) = {

  }

  def explode(basic: Basic) = {
    info(s"EXPLOSION${basic.position.x},${basic.position.y},${basic.position.z}")
  }

}