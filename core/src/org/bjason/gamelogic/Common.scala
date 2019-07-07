package org.bjason.gamelogic

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Pixmap
import org.bjason.gamelogic.basic.shape.Terrain

object Common {

  val CHANGED=2
  val UNCHANGED=0

  var currentTimeToLive:Long = 0L
  def refreshTtlTime = {
    currentTimeToLive = System.currentTimeMillis()
  }

  implicit class StringImprovements(val s: String) {
        def increment = s.map(c => (c + 1).toChar)
    }

  var XXterrain:Terrain=null

  var assets = new AssetManager(); ;

  def loadAssetsForMe(list:Array[ scala.Tuple2[String,Class[_]] ]) {

    for(l <- list) {
      assets.load(l._1,l._2)
      Log.info(s"loading ${l._1} ${l._2}")
    }

    assets.finishLoading()
  }



  def rotate(src:Pixmap ,angle:Float,srcX:Int,srcY:Int,width:Int,height:Int) = {

    val rotated = new Pixmap(width, height, src.getFormat());

    val radians = Math.toRadians(angle)
    val cos = Math.cos(radians)
    val sin = Math.sin(radians);

    if ( angle != 0 ) {
      for (x <- 0f to width.asInstanceOf[Float] by 1) {
          for (y <- 0f to height by 1) {

              val centerx = width/2
              val centery = height / 2
              val m = x - centerx
              val n = y - centery
              val j = (m * cos + n * sin) + centerx
              val k = (n * cos - m * sin) + centery
              if (j >= 0 && j < width && k >= 0 && k < height){
                var pixel = src.getPixel( (k+srcX).asInstanceOf[Int], (j+srcY).asInstanceOf[Int])
                rotated.drawPixel(width-x.asInstanceOf[Int], y.asInstanceOf[Int], pixel);
              }
          }
      }
    } else {
      rotated.drawPixmap(src, 0,0,srcX, srcY,width,height)
    }
    rotated;
  }


}
