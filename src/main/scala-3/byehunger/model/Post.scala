package byehunger.model

import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.image.Image

import java.time.{LocalDate, LocalDateTime}

abstract class Post(
                     val id: String,
                     var title: String,
                   ) {
  var description: String = _
  var dateCreated: LocalDateTime = LocalDateTime.now()
  var images: ObservableBuffer[String] = ObservableBuffer[String]()
  
  def addImage(image: String): Unit = {
    if (images.size < 3) images += image
    else println("Maximum 3 images allowed")
  }

  def removeImage(image: String): Unit = {
    images -= image
  }

  def generatePostID(): String = ???
}
