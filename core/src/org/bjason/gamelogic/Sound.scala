package org.bjason.gamelogic

import com.badlogic.gdx.Gdx

object Sound {


  lazy private val scoop = Gdx.audio.newMusic(Gdx.files.internal("data/scoop.wav"))
  lazy private val fire = Gdx.audio.newMusic(Gdx.files.internal("data/fire.wav"))
  lazy private val hit = Gdx.audio.newMusic(Gdx.files.internal("data/explosion.wav"))

  def create = {

    //sheepSound.setVolume(0.5f)
  }


  def playFire  {
    fire.play()
  }
  def playHit = {
    hit.play()
  }
  def playScoop = {
    scoop.play()
  }

}
