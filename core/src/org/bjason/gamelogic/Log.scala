package org.bjason.gamelogic

import org.bjason.gamelogic

object Log {

  var tag:String  = "none"

  import com.badlogic.gdx.Gdx

  //lazy val logFile = Gdx.files.local(GameSetup.playerPrefix+"_log.txt")
  lazy val logFile = Gdx.files.absolute("/src/gdx/gamelogic/logs/"+gamelogic.GameSetup.playerPrefix+"_log.txt")

  val when = java.time.LocalDateTime.now()
  def info(in: AnyRef*)  {
    val message = new StringBuilder
    for (s <- in) {
      if (s != null) message.append(s.toString())
    }
    if ( Gdx.app != null ) Gdx.app.log(tag, message.toString)
    else println(message.toString)
    //val o = s"(I) ${when} ${message.toString} \n"
    //print(o)
    //logFile.writeString(o,true)
  }
  def debug  (in: Any*)  {
    val message = new StringBuilder
    for (s <- in) {
      if (s != null) message.append(s.toString())
    }
    val o = s"(D) ${when} ${message.toString} \n"
    if ( Gdx.app != null ) Gdx.app.debug(tag, message.toString)
    //print(o)
    //logFile.writeString(o,true)
    /*
    if ( Gdx.app != null ) Gdx.app.debug(tag, message.toString)
    else info("debug ",message.toString)
    */
  }
  def tofile(in: AnyRef*)  {
    val message = new StringBuilder
    for (s <- in) {
      if (s != null) message.append(s.toString())
    }
    /*
    if ( Gdx.app != null ) Gdx.app.log(tag, message.toString)
    else println(message.toString)
    */
    val o = s"(I) ${when} ${message.toString} \n"
    logFile.writeString(o,true)
  }

}
