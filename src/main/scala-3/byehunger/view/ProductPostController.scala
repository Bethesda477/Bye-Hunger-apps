package byehunger.view

import javafx.fxml.FXML
import byehunger.MainApp
import javafx.event.ActionEvent
import javafx.scene.control.{Button, TextField}
import byehunger.model.{ProductPost, RestaurantPost}
import javafx.scene.control.Label
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.HBox
import javafx.stage.Stage
import scalikejdbc.*

class ProductPostController():
  
  @FXML
  private var productNameLabel: Label = null
  @FXML
  private var productDescriptionLabel: Label = null
  @FXML
  private var priceLabel: Label = null
  @FXML
  private var thumbnailContainer: HBox= null
  @FXML
  private var mainImageView: ImageView = null
  @FXML
  private var leftButton: Button = null
  @FXML
  private var rightButton: Button = null
  @FXML
  private var favouriteButton: Button = null
  @FXML
  private var addToCartButton: Button = null
  @FXML
  private var restaurantButton: Button = null


  var stage: Option[Stage] = None

  var dialogStage: Stage = null
  var onClicked = false

  private var currentPost: ProductPost = _
  private var currentImageIndex: Int = 0

  // Add to ProductPostController
  private var parentController: RestaurantPostController = _

  def setParentController(controller: RestaurantPostController): Unit =
    parentController = controller

  def post: ProductPost = currentPost

  def post_=(p: ProductPost): Unit =
    currentPost = p
    bindFields()
    // Show navigation button only when embedded in RestaurantPost
    setNavigationMode(parentController != null)

  def initialize(): Unit =
    if productNameLabel != null then productNameLabel.setText("")
    if productDescriptionLabel != null then productDescriptionLabel.setText("")
    if priceLabel != null then priceLabel.setText("")
    if thumbnailContainer != null then thumbnailContainer.getChildren.clear()
    if mainImageView != null then mainImageView.setImage(null)
    currentImageIndex = 0
    
  def bindFields(): Unit =
    if currentPost != null then {
      productDescriptionLabel.textProperty().bind(currentPost.productDescription)
      productNameLabel.textProperty().bind(currentPost.productName)
      priceLabel.textProperty().bind(currentPost.price.asString(s"RM%.2f"))
      //      refreshImages(currentPost.imageURLs)
      // start with first image
      currentImageIndex = 0
      if currentPost.imageURLs.nonEmpty then
        mainImageView.setImage(new Image(currentPost.imageURLs(currentImageIndex), true))

      // thumbnails
      thumbnailContainer.getChildren.clear()
      currentPost.imageURLs.zipWithIndex.foreach { case (url, idx) =>
        val thumb = new ImageView(new Image(url, 100, 75, true, true))
        thumb.setOnMouseClicked(_ => {
          currentImageIndex = idx
          mainImageView.setImage(new Image(url, true))
        })
        thumbnailContainer.getChildren.add(thumb)
      }


    }

  @FXML
  def handleLeft(): Unit =
    if currentPost != null && currentPost.imageURLs.nonEmpty then {
      currentImageIndex = (currentImageIndex - 1 + currentPost.imageURLs.size) % currentPost.imageURLs.size
      mainImageView.setImage(new Image(currentPost.imageURLs(currentImageIndex), true))
    }

  @FXML
  def handleRight(): Unit =
    if currentPost != null && currentPost.imageURLs.nonEmpty then {
      currentImageIndex = (currentImageIndex + 1 + currentPost.imageURLs.size) % currentPost.imageURLs.size
      mainImageView.setImage(new Image(currentPost.imageURLs(currentImageIndex), true))
    }

  @FXML
  def handleFavourite(): Unit =
    if currentPost != null then MainApp.addProductToFavourites(currentPost)


  @FXML
  def handleAddToCart(): Unit =
    if currentPost != null then MainApp.addToCart(currentPost)
  @FXML
  def handleRestaurantButton(): Unit =
    if parentController != null then
      parentController.switchToRestaurantView()
    else if stage.isDefined then
      // If no parent controller, close the window (for direct access)
      stage.get.close()

  // Add this method to ProductPostController
  def setNavigationMode(isEmbedded: Boolean): Unit =
    if restaurantButton != null then
      restaurantButton.setVisible(isEmbedded)

  @FXML
  def handleClose(action: ActionEvent): Unit =
    onClicked = true
    stage.foreach(x => x.close())

  def nullChecking(x: String) =
    x == null || x.length == 0