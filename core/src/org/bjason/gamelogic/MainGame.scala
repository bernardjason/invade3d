package org.bjason.gamelogic

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.{ApplicationAdapter, Gdx, InputMultiplexer}
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap

case class MainGame() extends ApplicationAdapter {
  val thiscontroller = Controller


  override def create() {

    val pm = new Pixmap(1,1, Pixmap.Format.RGBA8888)

    Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0))
    pm.dispose()

    thiscontroller.create

    val multiplexer = new InputMultiplexer();
    multiplexer.addProcessor(PlayerMovement);

    Gdx.input.setInputProcessor(multiplexer);

  }

  val fps = new com.badlogic.gdx.graphics.FPSLogger

  override def render() {

    Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    Gdx.gl.glClearColor(1f,1f,1f, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

    InputHandler.render
    thiscontroller.cam.update
    thiscontroller.render()

  }

  override def dispose() {
    Controller.dispose
  }



}