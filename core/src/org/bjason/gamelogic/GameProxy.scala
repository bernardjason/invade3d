package org.bjason.gamelogic

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.{ApplicationAdapter, Gdx}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table

object GameProxy extends ApplicationAdapter {
  var adapter:ApplicationAdapter=null
  var intro:ApplicationAdapter=null

  def setAdapter(to:ApplicationAdapter) = adapter=to

  def endGameScreen = {
    adapter = new EndScreen
    adapter.create()
  }

  override def create(): Unit = {
    adapter.create()
  }

  override def resize(width: Int, height: Int): Unit = {
    adapter.resize(width,height)
  }

  override def render(): Unit = {
    adapter.render()
  }

  override def dispose(): Unit = {
    adapter.dispose()
  }

}
