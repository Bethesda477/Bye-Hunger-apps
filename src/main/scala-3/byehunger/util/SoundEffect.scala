package byehunger.util

import javafx.scene.media.{Media, MediaPlayer}
import java.net.URL

object SoundEffect:
  
  private var cartAddPlayer: Option[MediaPlayer] = None
  private var paymentSuccessPlayer: Option[MediaPlayer] = None
  private var loginAudioPlayer: Option[MediaPlayer] = None
  private var favouriteAudioPlayer: Option[MediaPlayer] = None
  private var alertAudioPlayer: Option[MediaPlayer] = None
  private var openPostAudioPlayer: Option[MediaPlayer] = None
  
  def initialize(): Unit =
    try
      val loginAudio = getClass.getResource("/audios/loginSuccessful.mp3")
      if loginAudio != null then
        val media = new Media(loginAudio.toExternalForm)
        loginAudioPlayer = Some(new MediaPlayer(media))
        loginAudioPlayer.foreach(_.setVolume(0.7))

      val openAudio = getClass.getResource("/audios/openPost.mp3")
      if openAudio != null then
        val media = new Media(openAudio.toExternalForm)
        openPostAudioPlayer = Some(new MediaPlayer(media))
        openPostAudioPlayer.foreach(_.setVolume(0.4))

      val cartAddUrl = getClass.getResource("/audios/addToCart.mp3")
      if cartAddUrl != null then
        val media = new Media(cartAddUrl.toExternalForm)
        cartAddPlayer = Some(new MediaPlayer(media))
        cartAddPlayer.foreach(_.setVolume(0.4))

      val favouriteAudio = getClass.getResource("/audios/addToFavourite.mp3")
      if favouriteAudio != null then
        val media = new Media(favouriteAudio.toExternalForm)
        favouriteAudioPlayer = Some(new MediaPlayer(media))
        favouriteAudioPlayer.foreach(_.setVolume(0.4))

      val alertAudio = getClass.getResource("/audios/alertSound.mp3")
      if alertAudio != null then
        val media = new Media(alertAudio.toExternalForm)
        alertAudioPlayer = Some(new MediaPlayer(media))
        alertAudioPlayer.foreach(_.setVolume(0.2))

      val paymentUrl = getClass.getResource("/audios/paySuccessful.mp3")
      if paymentUrl != null then
        val media = new Media(paymentUrl.toExternalForm)
        paymentSuccessPlayer = Some(new MediaPlayer(media))
        paymentSuccessPlayer.foreach(_.setVolume(0.8))
    catch
      case ex: Exception =>
        println(s"Failed to initialize sound effects: ${ex.getMessage}")

  def playLoginAudio(): Unit =
    loginAudioPlayer.foreach{ player =>
      player.stop()
      player.play()
    }

  def playOpenPostAudio(): Unit =
    openPostAudioPlayer.foreach { player =>
      player.stop()
      player.play()
    }

  def playFavouriteAudio(): Unit =
    favouriteAudioPlayer.foreach{ player =>
      player.stop()
      player.play()
    }

  def playAlertAudio(): Unit =
    alertAudioPlayer.foreach { player =>
      player.stop()
      player.play()
    }


  def playCartAdd(): Unit =
    cartAddPlayer.foreach { player =>
      player.stop()
      player.play()
    }
  
  def playPaymentSuccess(): Unit =
    paymentSuccessPlayer.foreach { player =>
      player.stop()
      player.play()
    }
  
  def dispose(): Unit =
    cartAddPlayer.foreach(_.dispose())
    paymentSuccessPlayer.foreach(_.dispose())
    cartAddPlayer = None
    paymentSuccessPlayer = None
    loginAudioPlayer.foreach(_.dispose())
    loginAudioPlayer = None
    favouriteAudioPlayer.foreach(_.dispose())
    favouriteAudioPlayer = None
    alertAudioPlayer.foreach(_.dispose())
    alertAudioPlayer = None
    openPostAudioPlayer.foreach(_.dispose())
    openPostAudioPlayer = None
  
//  // 测试音效功能
  def testSoundEffects(): Unit =
    println("=== sound effect testing ===")
    println(s"addToCart sound effect: ${if cartAddPlayer.isDefined then "started" else "not started"}")
    println(s"pay successful sound effect: ${if paymentSuccessPlayer.isDefined then "started" else "not started"}")

    if cartAddPlayer.isDefined then
      println("audio testing...")
      playCartAdd()
    else
      println("audio not working")

    if paymentSuccessPlayer.isDefined then
      println("audio testing...")
      playPaymentSuccess()
    else
      println("audio not working")

