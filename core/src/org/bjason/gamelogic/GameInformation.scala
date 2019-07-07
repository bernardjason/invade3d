package org.bjason.gamelogic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.{Batch, BitmapFont}
import org.bjason.gamelogic
import org.bjason.socket.{GameMessage, Websocket}

object GameInformation {


  var end = false;

  val allScore = scala.collection.mutable.Map[String, String]()

  def gotScore(m: GameMessage) = {
    val dropScore = m.msg.substring(Websocket.SCORE.length + 1)

    allScore += (m.objId -> dropScore)
  }

  def allScoreFinal = {
    var you = ""
    val others = allScore.values.filter{  s =>
      if ( s.startsWith("Player " + gamelogic.GameSetup.playerPrefix + " ")) {
        you = s
        false
      } else true
    }
    you :: others.toList
  }

  def send = {
    s"SCORE Player ${gamelogic.GameSetup.playerPrefix} score ${score} hits ${playerHits}"
  }


  lazy val font = new BitmapFont();


  private var playerHits = 0;
  private var score = 0;
  private var aliens = 0;
  private var countDown = 180
  lazy val drawHeightY = Gdx.graphics.getHeight * 0.9f

  def setAliens(are: Int): Unit = {
    aliens = are
    if (aliens <= 0) countDown = countDown - 1
  }

  def isGameEnd = {
    if ((aliens <= 0 && countDown <= 0) || end == true) true else false
  }

  def playerHit(): Unit = {
    playerHits = playerHits + 1
  }

  def addScore(by: Int) = {
    score = score + by
  }

  def drawText(batch: Batch): Unit = {

    font.setColor(Color.WHITE)
    font.draw(batch, s"Player hits ${playerHits} score ${score}  ${aliens}", 10, drawHeightY)
    var y = drawHeightY - 40
    for (m <- allScore) {
      font.setColor(Color.YELLOW)
      font.draw(batch, m._2, 10, y)
      y = y - 25
    }
  }
}
