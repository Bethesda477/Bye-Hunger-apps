package byehunger.view

import javafx.fxml.FXML
import scalafx.scene.control.Alert
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.HBox
import javafx.stage.{FileChooser, Stage}
import scalafx.collections.ObservableBuffer
import scalafx.Includes.*
import scalafx.scene.control.Alert

import java.io.File

trait ImageManagement:
  
  // Abstract methods that implementing classes must provide
  def getImageUrls: ObservableBuffer[String]
  def getStage: Option[Stage]
  def getImagePreviewPane: HBox
  
  // Maximum number of images allowed
  protected val maxImages: Int = 3
  
  /** Open FileChooser to select an image */
  @FXML
  def handleAddImage(): Unit =
    if getImageUrls.size >= maxImages then
      new Alert(Alert.AlertType.Warning):
        initOwner(getStage.orNull)
        title = "Image Limit"
        headerText = "Maximum Images Reached"
        contentText = s"You can only add up to $maxImages images."
      .showAndWait()
      return

    val fileChooser = new FileChooser()
    fileChooser.setTitle("Choose an Image")
    fileChooser.getExtensionFilters.addAll(
      new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
    )
    val file = fileChooser.showOpenDialog(getStage.orNull)
    if file != null then addImageFromFile(file)

  /** Helper: Add file to list & refresh UI */
  def addImageFromFile(file: File): Unit =
    val path = file.toURI.toString
    getImageUrls += path
    refreshImagePreview()

  /** Update the HBox to show all thumbnails */
  def refreshImagePreview(): Unit =
    getImagePreviewPane.getChildren.clear()
    getImageUrls.foreach { url =>
      val imgView = new ImageView(url)
      imgView.setFitWidth(100)
      imgView.setFitHeight(75)
      imgView.setPreserveRatio(true)
      getImagePreviewPane.getChildren.add(imgView)
    }

  /** Clear all images */
  def clearImages(): Unit =
    getImageUrls.clear()
    refreshImagePreview()

  /** Get the number of current images */
  def getImageCount: Int = getImageUrls.size

  /** Check if image limit is reached */
  def isImageLimitReached: Boolean = getImageUrls.size >= maxImages

  /** Remove image at specific index */
  def removeImage(index: Int): Unit =
    if index >= 0 && index < getImageUrls.size then
      getImageUrls.remove(index)
      refreshImagePreview()

  /** Get all image URLs as a sequence */
  def getAllImageUrls: Seq[String] = getImageUrls.toSeq
  
  /** Setup drag and drop functionality for image preview pane */
  def setupDragAndDrop(): Unit =
    getImagePreviewPane.setOnDragOver(event => {
      if event.getDragboard.hasFiles then
        event.acceptTransferModes(javafx.scene.input.TransferMode.COPY_OR_MOVE)
      event.consume()
    })

    getImagePreviewPane.setOnDragDropped(event => {
      val db = event.getDragboard
      if db.hasFiles then
        db.getFiles.forEach { file =>
          if getImageUrls.size < maxImages then
            addImageFromFile(file)
        }
      event.setDropCompleted(true)
      event.consume()
    })
