package org.bjason.gamelogic

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.{Gdx, InputAdapter}

object InputHandler extends InputAdapter {

  val direction = new Vector3(0, 0, 1);
  val oldDirection = new Vector3(0, 0, 0)
  val userAction = new Vector3(0, 0, 0);
  private var fire = false
  var timeSinceLastFire = 0L
  val GAP_BETWEEN_FIRE = 100

  var dir = 0f;
  var up = 0f;
  val ROTATE = 1f;
  var speed = 0f;

  def rollback: Unit = {
    direction.set(oldDirection)
  }

  var testCounter = 1f

  def render = {

    oldDirection.set(direction)
    userAction.setZero()
    if (dir != 0f) {
      direction.rotate(dir, 0, 1, 0)
    }
    if (speed != 0) {
      userAction.set(direction).scl(speed)
    }
    if (up != 0) {
      userAction.y = up;
    }

  }

  def stopPlayer = {
    speed = 0
    dir = 0
    up = 0
  }

  def isFireAndUnset = {
    var r = false
    if (fire) {
      val now = System.currentTimeMillis()
      if (now > timeSinceLastFire + GAP_BETWEEN_FIRE) {
        timeSinceLastFire = now
        r = true
      }
    }
    r
  }

  override def keyUp(c: Int): Boolean = {
    c match {
      case Keys.LEFT | Keys.RIGHT => dir = 0;
      case Keys.UP | Keys.DOWN => up = 0;
      case Keys.SHIFT_LEFT | Keys.SHIFT_RIGHT => speed = 0f
      case Keys.SPACE => fire = false
      case _ =>
    }
    false;
  }

  override def keyDown(c: Int): Boolean = {
    if (Gdx.input.isKeyPressed(Keys.LEFT)) {
      dir = ROTATE;
    }
    if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
      dir = -ROTATE;
    }
    if (Gdx.input.isKeyPressed(Keys.UP)) {
      up = -1;
    }
    if (Gdx.input.isKeyPressed(Keys.DOWN)) {
      up = 1;
    }

    if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) == true) {
      speed = 1f
    }
    if (Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT) == true) {
      speed = -1f
    }
    if (Gdx.input.isKeyPressed(Keys.SPACE) == true) {
      fire = true
    }
    if (Gdx.input.isKeyPressed(Keys.ESCAPE) == true) {
      System.exit(0)
    }

    false
  }

}
