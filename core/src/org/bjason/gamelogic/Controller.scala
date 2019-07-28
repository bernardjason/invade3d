package org.bjason.gamelogic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.{DirectionalLight, DirectionalShadowLight}
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider
import com.badlogic.gdx.graphics.g3d.{Environment, Model, ModelBatch}
import com.badlogic.gdx.graphics.{FPSLogger, PerspectiveCamera, Pixmap, Texture}
import com.badlogic.gdx.math.{Quaternion, Vector3}
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.collision._
import org.bjason.gamelogic.Log._
import org.bjason.gamelogic.basic.move
import org.bjason.gamelogic.basic.shape.{AlienGround, AlienMissileShape, Basic, BulletShape, CollideShape, FuelBase, MyContactListener, PlayerSprite, Terrain}
import org.bjason.socket.{GameMessage, State, Websocket}
import org.bjason.{gamelogic, socket}

import scala.collection.mutable.ArrayBuffer

object Controller {

  Bullet.init(false, true)

  val myAssets: Array[scala.Tuple2[String, Class[_]]] = Array(
    ("data/basic.jpg", classOf[Texture]),
    ("data/landscape.jpg", classOf[Texture]),
    ("data/cuboid.jpg", classOf[Pixmap]),
    ("data/alienmissile.jpg", classOf[Texture]),
    ("data/aliencube.jpg", classOf[Pixmap]),
    ("data/fuelbase.png", classOf[Texture]),
    ("data/surface.png", classOf[Texture]),
    ("data/explosion.jpg", classOf[Pixmap]),
    ("data/sky.png", classOf[Pixmap]),
    ("data/invade.g3db", classOf[Model]),
    ("data/alien.g3db", classOf[Model])
  )


  lazy val environment = new Environment()
  lazy val cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
  val objects: ArrayBuffer[Basic] = ArrayBuffer()

  val deadlist: ArrayBuffer[Basic] = ArrayBuffer()
  val newlist: ArrayBuffer[Basic] = ArrayBuffer()
  lazy val modelBatch = new ModelBatch()
  lazy val spriteBatch = new SpriteBatch();

  lazy val shadowLight = new DirectionalShadowLight(2048, 2048, 1060f, 1460f, .1f, 550f)
  lazy val shadowBatch = new ModelBatch(new DepthShaderProvider());


  lazy val player = PlayerSprite(new Vector3(10, 30, 10), movement = PlayerMovement)
  lazy val skyTexture = new Texture(Gdx.files.internal("data/sky.png"))
  lazy val skyWidth = skyTexture.getWidth
  lazy val skyHeight = skyTexture.getHeight

  val collisionConfig = new btDefaultCollisionConfiguration()
  val dispatcher = new btCollisionDispatcher(collisionConfig)
  val broadphase = new btDbvtBroadphase()
  val collisionWorld = new btCollisionWorld(dispatcher, broadphase, collisionConfig)
  val contactListener = new MyContactListener()
  val shape: ArrayBuffer[CollideShape] = ArrayBuffer()
  val bulletlist: ArrayBuffer[CollideShape] = ArrayBuffer()
  val mycontactLisstener = new MyContactListener

  def addBulletObjectToDispose(bulletObject: CollideShape) {
    bulletlist += bulletObject
  }

  def doBulletDispose {
    for (b <- bulletlist) {
      b.dispose
    }
    bulletlist.clear
    collisionWorld.release()
  }

  val terrains = scala.collection.mutable.Map[String, Terrain]()
  val master = new MasterInvader(startPosition =  new Vector3(0,300,0))
  var lastPitUsed = 0
  val doTerrain = false

  objects ++= Array(
    player,
    master
  )

  def maxObjects = objects.size

  def create {
    gamelogic.Common.loadAssetsForMe(myAssets)
    socket.Websocket.test

    cameraEnvironment
    PlayerMovement.init(player)

    Gdx.graphics.setTitle("Player " + Basic.playerPrefix)

    info("Game " + "Player " + Basic.playerPrefix + "  starting")

    val groundScale = 256
    for( groundx <- -groundScale*2 to groundScale*2 by groundScale ) {
      for(groundz <- -groundScale*2 to groundScale*2 by groundScale ) {
        val ground = new AlienGround(startPosition = new Vector3(groundx, 0, groundz))
        objects += ground
      }
    }

    val spaceBetweenFueld=25
    val MAX_BASES=32
    val baseStart = MAX_BASES/2/2/2
    for( fz <- -baseStart to baseStart ) {
      for ( fx <- -baseStart to baseStart ) {
        val fuelBase = new FuelBase(id=s"C_${fz}_${fx}",startPosition =  new Vector3(fx * spaceBetweenFueld,0,fz * spaceBetweenFueld))
        objects += fuelBase
      }
    }

    if (doTerrain) {
      for (t <- terrains.values) {
        t.init
        t.reset
      }
    }

    for (o <- objects) o.init

    for (o <- objects) o.reset

    socket.Websocket.connect(socket.Websocket.Login(gamelogic.GameSetup.playerPrefix.toString, "" + gamelogic.GameSetup._playerId, gamelogic.GameSetup.gameName))

    for(o <- objects ) {
      o.jsonObject.map{ j =>
        socket.Websocket.jsonobjects += j

      }
    }
    GameInformation.startGame

  }

  val fps = new FPSLogger()
  var ticker = 0
  var skyAddX = 0f

  def render() {

    Sound.invading
    Websocket.clearDeletedObjects
    socket.Websocket.sayHello
    socket.Websocket.writeAllMyObjects()
    socket.Websocket.readOtherObjects()


    gamelogic.Common.refreshTtlTime
    ticker = ticker + 1

    if (ticker % 60 == 0) {
      Websocket.broadcastMessage(GameMessage(objId = "" + gamelogic.GameSetup.playerPrefix, msg = GameInformation.send))
    }

    val q = new Quaternion()
    player._getRotation(q)
    val angleAround = q.getAngleAround(0,1,0)
    val add = new Vector3(0,0,200)
    add.rotate(angleAround,0,1,0)
    add.add(player.position)
    add.y = player.position.y
    cam.position.set(add)

    cam.lookAt(player.position)
    cam.position.y = player.position.y + 50
    cam.update()

    skyAddX = skyAddX + PlayerMovement.dir*20
    val forSkyY = player.position.y
    val forSkyX = skyAddX
    spriteBatch.begin()

    for (skyY <- (forSkyY % skyHeight).toInt - skyHeight*2 to (forSkyY % skyHeight).toInt + skyHeight by skyHeight) {
      for (skyX <- (forSkyX % skyWidth) - skyWidth to (forSkyX % skyWidth) + skyWidth * 2 by skyWidth) {
        spriteBatch.draw(skyTexture, skyX, skyHeight - skyY)
      }
    }
    spriteBatch.end()

    if (doTerrain) addSurroundingTerrains

    for (o <- objects) {
      o.move(objects.toList)
    }

    for (o <- objects) {

      if (o.shape.bullet && o.shape.bullet) {
        val s = o.shape.asInstanceOf[BulletShape]
        if (s.bulletObject != null) s.bulletObject.setWorldTransform(o._getTransform)

      }
    }

    collisionWorld.performDiscreteCollisionDetection()




    doShadow()
    modelBatch.begin(cam)

    if (doTerrain) drawTheTerrains

    for (o <- objects) {
      if (o.shape.isVisible(o.instance.transform, cam)) {
        o._render(modelBatch, environment, cam)
      }
    }

    modelBatch.end()

    spriteBatch.begin()
    GameInformation.drawText(spriteBatch)
    spriteBatch.end()


    doDeadList
    doNewList
    doBulletDispose

    fps.log()

    //GameInformation.setAliens( Websocket.jsonobjects.filter( _.what == "EightBitInvader").size )

    if (GameInformation.isGameEnd) {
      GameProxy.endGameScreen
    }
  }


  val tempPosition = new Vector3

  def doShadow() = {
    player.instance.transform.getTranslation(tempPosition)
    tempPosition.y = tempPosition.y - 100
    shadowLight.begin(tempPosition, cam.direction);
    shadowBatch.begin(shadowLight.getCamera());
    shadowBatch.render(player.instance)


    objects.filter( _.isInstanceOf[AlienMissileShape]).map( o => shadowBatch.render(o.instance))
    if (doTerrain) drawShadowTerrain

    shadowBatch.end();
    shadowLight.end();
  }


  private def drawTheTerrains = {
    val drawTerrains = 1
    for (zz <- -Terrain.terrainSize * drawTerrains to Terrain.terrainSize * drawTerrains by Terrain.terrainSize) {
      for (xx <- -Terrain.terrainSize * drawTerrains to Terrain.terrainSize * drawTerrains by Terrain.terrainSize) {
        terrains.get(Terrain.positionToKey(player.position.x + xx, player.position.z + zz)).map { t1 =>
          t1.move()
          t1._render(modelBatch, environment, cam)
        }
      }
    }
  }

  private def drawShadowTerrain = {
    val drawTerrains = 1
    for (zz <- -Terrain.terrainSize * drawTerrains to Terrain.terrainSize * drawTerrains by Terrain.terrainSize) {
      for (xx <- -Terrain.terrainSize * drawTerrains to Terrain.terrainSize * drawTerrains by Terrain.terrainSize) {
        terrains.get(Terrain.positionToKey(player.position.x + xx, player.position.z + zz)).map { t1 =>
          t1.renderShadow(shadowBatch, cam)
        }
      }
    }
  }

  val EASIER_TO_DEBUG = (Terrain.terrainSize * 1).toInt

  private def addSurroundingTerrains = {
    val give = EASIER_TO_DEBUG
    for (zzz <- player.position.z.toInt - give to player.position.z.toInt + give by Terrain.terrainSize) {
      for (xxx <- player.position.x.toInt - give to player.position.x.toInt + give by Terrain.terrainSize) {
        val x = player.position.x + xxx
        val z = player.position.z + zzz
        if (x > -Terrain.MAXX + Terrain.terrainSize && x < Terrain.MAXX - Terrain.terrainSize &&
          z > -Terrain.MAXZ + Terrain.terrainSize && z < Terrain.MAXZ - Terrain.terrainSize) {

          val key = Terrain.positionToKey(x, z)
          terrains.getOrElseUpdate(key, createTerrainForKey(x, z, key))
        } else {
          val key = Terrain.positionToKey(x, z)
          terrains.getOrElseUpdate(key, {
            val v3 = Terrain.positionToKeyVector3(x, z)
            v3.scl(Terrain.terrainSize)
            val t = new Terrain(key, v3, "data/landscape.jpg", land = false)
            t.init
            t.reset
            t
          }
          )

        }
      }
    }
  }

  def createTerrainForKey(x: Float, z: Float, key: String) = {

    val v3 = Terrain.positionToKeyVector3(x, z)
    v3.scl(Terrain.terrainSize)
    val t = new Terrain(key, v3, "data/landscape.jpg")
    t.init
    t.reset
    t

  }

  def dispose {
    modelBatch.dispose()

  }

  def changeFocus(objectNumber: Int) {
    for (o <- objects) o.movement = move.NoMovement

    objects(objectNumber).movement = move.Keyboard
  }


  def cameraEnvironment {
    environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
    environment.set(new ColorAttribute(ColorAttribute.Fog, 0.48f, 0.69f, 1f, 0.18f))
    environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))

    environment.add(shadowLight.set(0.8f, 0.8f, 0.8f, -0f, -0.8f, -0.1f))
    environment.shadowMap = shadowLight
    cam.position.set(0, 10, -50)
    cam.lookAt(0, 0, 0)
    cam.near = 1f
    cam.far = 1000
    cam.update()

  }


  def addNewBasic(basic: Basic) {
    newlist += basic
  }

  def addToDead(remove: Basic) {
    deadlist += remove
    remove.jsonObject.map { x => x.state = Some(State.DEAD) }
  }

  def doDeadList {
    for (d <- deadlist) {
      objects -= d
      addBulletObjectToDispose(d.shape)
      d.dead = true
    }
    deadlist.clear()
  }

  def doNewList {
    for (b <- newlist) {
      b.init
      b.reset
      objects += b
    }
    newlist.clear()
  }

  def pause() {
    System.exit(0)
  }


}
