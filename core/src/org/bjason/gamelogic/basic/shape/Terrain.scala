package org.bjason.gamelogic.basic.shape

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.{Environment, ModelBatch}
import com.badlogic.gdx.math.Vector3
import org.bjason.gamelogic.basic.move.NoMovement
import org.bjason.socket.Websocket

import scala.collection.mutable.ArrayBuffer

case class Terrain(key: String, override val position: Vector3, textureName: String, land: Boolean = true) extends BaseTerrain(position, textureName) {
  val blockSize: Int = Terrain.blockSize
  val terrainGroundZero: Int = 0
  val terrainSize: Int = Terrain.terrainSize
  val MAX = 2
  val rollbackScale = 0f
  val objects: ArrayBuffer[Basic] = ArrayBuffer()
  val invaderStart = position.cpy
  val invaders: scala.collection.mutable.ListBuffer[EightBitInvader] = scala.collection.mutable.ListBuffer()


  override def setupHeightMatrix: Unit = {

    var jiggle = (Math.random() * 1000).toInt
    val offFromSide = 8
    var up = true
    for (zz <- 0 until terrainSize / blockSize) {
      jiggle = jiggle + 1
      for (xx <- 0 until terrainSize / blockSize) {
        val r = jiggle % 2
        jiggle = jiggle + 1
        matrix(zz)(xx) = new AtPosition(height = 0, terrainTextureOffset = r, desc = "water")
      }
    }
    for (zz <- 1 until terrainSize / blockSize - 1) {
      for (xx <- 1 until terrainSize / blockSize - 1) {
        val r = jiggle % 2
        jiggle = jiggle + 1

        var diff = (Math.random() * 5f).asInstanceOf[Float]
        if (matrix(zz - 1)(xx - 1).height > terrainGroundZero + MAX) up = false
        if (matrix(zz - 1)(xx - 1).height <= terrainGroundZero) up = true
        if (up == false) diff = diff * -1

        matrix(zz)(xx) = new AtPosition(height = 0, terrainTextureOffset = r, desc = "water")
        matrix(zz)(xx).height = matrix(zz - 1)(xx - 1).height + diff

      }
      jiggle = jiggle + 1
    }


    if (land) {

      defaultLand(jiggle, offFromSide)

      addFromFile(jiggle)

    }

  }

  def addFromFile(jig: Int) {
    var jiggle = jig
    val is = Gdx.files.internal(s"data/cell.txt").read()


    def defaultCell = {
      val r = jiggle % 2
      jiggle = jiggle + 1
      s"0,${r},water"
    }

    val s = scala.io.Source.fromInputStream(is)
    var zz = Terrain.terrainSize / Terrain.blockSize - 1
    for (line <- s.getLines()) {
      jiggle = (Math.random() * 1000).toInt
      var xx = Terrain.terrainSize / Terrain.blockSize - 1
      val terminated = line + "X"
      // split would stop at last populated field. Odd hence the "x"
      terminated.replaceAll(" ", "").split("[|]").map {
        cell => (if (cell.length > 0) cell else defaultCell).split(",")
      }.foreach { str =>
        if (xx >= 0 && zz >= 0) {
          str match {
            case Array("X") =>
            case Array(height, terrainTextureOffset, desc, terrainObject) =>
              matrix(zz)(xx) = AtPosition(height = height.toFloat, terrainTextureOffset = terrainTextureOffset.toInt, desc = desc, objs = List(terrainObject))
            case Array(terrainTextureOffset) => matrix(zz)(xx).terrainTextureOffset = terrainTextureOffset.toInt
            case Array(height, terrainTextureOffset) =>
              matrix(zz)(xx).terrainTextureOffset = terrainTextureOffset.toInt
              matrix(zz)(xx).height = height.toFloat
            case Array(height, terrainTextureOffset, desc) =>
              matrix(zz)(xx).terrainTextureOffset = terrainTextureOffset.toInt
              matrix(zz)(xx).height = height.toFloat
              matrix(zz)(xx).desc = desc
            case a: Array[String] => a.toList
              val height = a(0).toFloat
              val texOffset = a(1).toInt
              val desc = a(2)
              val objs = a.drop(3).toList
              matrix(zz)(xx) = new AtPosition(height = height, terrainTextureOffset = texOffset, desc = desc, objs = objs)
            case _ =>
          }
          matrix(zz)(xx).objs.map { obj =>
            val where = new Vector3(xx * Terrain.blockSize, 0, zz * Terrain.blockSize)
            where.add(position)
            where.sub(Terrain.terrainSize / 2, 0, Terrain.terrainSize / 2)
            where.add(Terrain.blockSize / 2, 0, Terrain.blockSize / 2)
            addObject(matrix(zz)(xx).desc, obj, where, s"${key}${Terrain.SEPARATOR}${zz}${xx}")
          }
        }
        xx = xx - 1
      }
      zz = zz - 1
    }
  }


  def defaultLand(jig: Int, offFromSide: Int) {
    var jiggle = jig
    var up = true
    val baseGround = 10
    val maxGround = 20
    val radius = terrainSize / blockSize / 2.5

    for (angle <- 0 until 360) {
      val zz = ((Math.sin(Math.toRadians(angle)) * radius).toInt + terrainSize / blockSize / 2)
      val xx = ((Math.cos(Math.toRadians(angle)) * radius).toInt + terrainSize / blockSize / 2)
      val r = jiggle % 10
      jiggle = jiggle + 1
      matrix(zz)(xx) = new AtPosition(height = 0, terrainTextureOffset = 2 + r, desc = "land")
    }


    var fill = true
    val middle = terrainSize / blockSize / 2
    var randomList = List( 1,2,4,5,3,2,2,6)
    var randomIndex=0
    def getRandom = {
      val r:Float = randomList(randomIndex)
      randomIndex=randomIndex+1
      if ( randomIndex >= randomList.length ) randomIndex=0
      r
    }

    def fillIt(xx: Int, zz: Int) = {
      val r = jiggle % 10
      jiggle = jiggle + 1
      if (fill == true && matrix(zz)(xx).desc == "land") fill = false
      if (fill == true) {
        matrix(zz)(xx) = new AtPosition(height = 0, terrainTextureOffset = 2 + r, desc = "land")
        var diff = getRandom
        if (matrix(zz - 1)(xx - 1).height > maxGround) up = false
        if (matrix(zz - 1)(xx - 1).height <= baseGround) up = true
        if (up == false) diff = diff * -1
        matrix(zz)(xx).height = matrix(zz - 1)(xx - 1).height + diff
      }
      fill
    }

    def moveAcrossAndFill(startX:Int,end:Int,step:Int): Unit = {
      var xx = startX
      fill=true
      while( xx != end ) {
        val atXFill = fillIt(xx, middle)
        if (atXFill) {
          for (zz <- middle + 1 until middle * 2) fillIt(xx, zz)
          fill = true
          for (zz <- middle - 1 until 0 by -1) fillIt(xx, zz)
        }
        jiggle = jiggle + 1
        fill = atXFill
        xx=xx+step
      }
    }

    moveAcrossAndFill(middle,middle*2,1)
    moveAcrossAndFill(middle-1,0,-1)

  }

  def addObject(desc: String, what: String, where: Vector3, key: String): Unit = {
    what match {
      case "cuboid" =>
        addCuboid(desc, where, key)
      case "ball" =>
        invaderStart.set(where)
        for (xx <- 1 to 4) {
          for (zz <- 0 to 2) {
            val place = where.cpy
            place.y = 100
            place.x = place.x - xx * 80
            place.z = place.z - zz * 80
            val invader = addEightBitInvader(desc, place, s"C_${key}!${zz}-${xx}")
            invaders += invader
          }
        }

        //if ( GameSetup.playerPrefix%2 == 0 ) invaders.head.moving = true
        invaders.head.moving = true
      case _ =>
    }

  }

  private def addCuboid(desc: String, where: Vector3, key: String) = {
    val dim = desc.split("x").map(x => Integer.parseInt(x))
    val sizes = new Vector3(dim(0), dim(1), dim(2))
    where.y = dim(1)
    val c = new Cuboid(textureName = "data/cuboid.jpg", startPosition = where, dimensions = sizes, radius = dim.sum,
      movement = NoMovement, id = key + "_cube")
    c.init
    c.reset
    objects += c
    Websocket.jsonobjects += c.jsonObject.get
  }

  private def addEightBitInvader(desc: String, where: Vector3, id: String) = {
    //val dim = desc.split("x").map(x => Integer.parseInt(x))
    //where.y = where.y + dim(1) * 2
    println(id)
    val c = new EightBitInvader(startPosition = where, radius = 20, id = id)
    c.init
    c.reset
    objects += c
    Websocket.jsonobjects += c.jsonObject.get
    c
  }

  var alienrow = 0
  var aliencol = 0
  val landedAlienSize = 40
  val maxAlienCells = 4

  var invadersDone = false

  val remove = ArrayBuffer[Basic]()

  def move() = {
    remove.clear()
    for (o <- objects) {
      o.move(objects.toList)
      if (o.dead) remove += o
    }
    remove.map { o =>
      //println("*************** BERNARD DEAD " + o.id + " " + o)
      objects -= o
    }

    var nextOneMove = false
    for (o <- invaders) {
      if (nextOneMove) {
        nextOneMove = false
        o.moving = true
      }
      if (o.moving == true && o.dead == true) {
        nextOneMove = true
        o.onDead.foreach(oo => objects += oo)
      }
      if (o.dead) invaders -= o
    }
    if (invaders.isEmpty) invadersDone = true
  }


  override def _render(modelBatch: ModelBatch, environment: Environment, cam: Camera) = {

    if (display) {
      modelBatch.render(instance, environment)
      for (o <- objects) {
        if (o.shape.isVisible(o.instance.transform, cam) && !o.dead) {
          modelBatch.render(o.instance, environment)
        }
      }
      objects.filter(o => o.dead).foreach(objects -= _)
    }
  }

  def renderShadow(modelBatch: ModelBatch, cam: Camera) = {
    for (o <- objects) {
      if (o.shape.isVisible(o.instance.transform, cam) && !o.dead) {
        modelBatch.render(o.instance)
      }
    }
  }

}

object Terrain {
  val MAXX = 1024
  val MAXZ = 1024
  val terrainsMultiplier = 2
  val terrainSize: Int = 512
  val blockSize: Int = 16
  val ratio = terrainSize / blockSize
  val SEPARATOR = "!"

  val cellMax = terrainSize / blockSize

  def key(x: Int, z: Int): String = {
    s"${z}_${x}"
  }

  def positionToKey(x: Float, z: Float): String = {
    val offx = if (x < 1) -(terrainSize / 2) else (terrainSize / 2)
    val offz = if (z < 0) -(terrainSize / 2) else (terrainSize / 2)
    key(
      ((x + offx).toInt / (terrainSize)).toInt,
      ((z + offz).toInt / (terrainSize)).toInt)
  }

  def positionToKeyVector3(x: Float, z: Float): Vector3 = {
    val offx = if (x < 1) -(terrainSize / 2) else (terrainSize / 2)
    val offz = if (z < 0) -(terrainSize / 2) else (terrainSize / 2)
    new Vector3(((x + offx).toInt / (terrainSize)).toInt,
      0,
      ((z + offz).toInt / (terrainSize)).toInt
    )
  }

}



