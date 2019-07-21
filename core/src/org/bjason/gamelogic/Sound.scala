package org.bjason.gamelogic

import com.badlogic.gdx.Gdx

object Sound {


  lazy private val scoop = Gdx.audio.newMusic(Gdx.files.internal("data/scoop.wav"))
  lazy private val alienMissileSound = Gdx.audio.newMusic(Gdx.files.internal("data/alienmissile.wav"))
  lazy private val fire = Gdx.audio.newMusic(Gdx.files.internal("data/fire.wav"))
  lazy private val hit = Gdx.audio.newMusic(Gdx.files.internal("data/explosion.wav"))
  lazy private val background1 = Gdx.audio.newMusic(Gdx.files.internal("data/background1.wav"))
  lazy private val background2 = Gdx.audio.newMusic(Gdx.files.internal("data/background2.wav"))
  lazy private val background3 = Gdx.audio.newMusic(Gdx.files.internal("data/background3.wav"))

  lazy val allBackgrounds = List(background1,background1,background2,background2,background3)
  def create = {
  }
  var levelSound=0

  /*
  def nextLevel = {
    allBackgrounds.map{ s =>
      s.setLooping(true)
      s.stop()
    }
    allBackgrounds(levelSound).play()
    levelSound=levelSound+1
    if ( levelSound >= allBackgrounds.length ) levelSound = allBackgrounds.length-1
  }
   */
  def allStop = {
    alienMissileSound.stop()
  }

  def level(level:Int): Unit = {
    this.levelSound = level/200
  }
  private var countDown = 0f
  def invading = {
    if ( countDown <= 0 ) {
      background3.play()
      countDown = levelSound
    }
    countDown = countDown - Gdx.graphics.getDeltaTime
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
  def alienMissile = {
    alienMissileSound.setLooping(true)
    alienMissileSound.play()
  }
  def soptAlienMissileShape = {
    alienMissileSound.pause

  }

}
