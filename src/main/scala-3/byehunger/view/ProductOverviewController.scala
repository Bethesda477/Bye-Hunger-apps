package byehunger.view

import javafx.fxml.{FXML, FXMLLoader}
import byehunger.MainApp
import byehunger.model.ProductPost
import javafx.event.ActionEvent
import javafx.scene.control.{Button, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{BorderPane, HBox, Pane, VBox}
import javafx.stage.Stage
import scalafx.Includes.jfxStage2sfx

@FXML
class ProductOverviewController():
  @FXML
  private var window: BorderPane = _
  @FXML
  private var leftButton: Button = _
  @FXML
  private var rightButton: Button = _
  @FXML
  private var otherProductStrip: HBox = _
  @FXML
  private var restaurantButton: Button = _



  var productPostController: Option[ProductPostController] = None

  private var relatedProducts: Seq[ProductPost] = Seq.empty
  private var stripStartIndex: Int = 0
  private val visibleCount: Int = 4

  private var isEmbeddedMode: Boolean = false
  private var parentController: RestaurantPostController = _

  var stage: Option[Stage] = None

  var dialogStage: Stage = null
  var onClicked = false

  private var currentPost: ProductPost = _
  private var currentImageIndex: Int = 0

  def post: ProductPost = currentPost

  def post_=(p: ProductPost): Unit =
    currentPost = p
    // load the center view for this product immediately
    loadView[ProductPostController]("/byehunger/view/ProductPost.fxml").foreach(_.post = p)
    refreshRelatedProducts()

  // Add this method to set embedded mode
  def setEmbeddedMode(embedded: Boolean, parent: RestaurantPostController = null): Unit =
    isEmbeddedMode = embedded
    parentController = parent
    if restaurantButton != null then
      restaurantButton.setVisible(true) // Always show the button

  // Add the restaurant button handler
  @FXML
  def handleBackToRestaurant(): Unit =
    if isEmbeddedMode && parentController != null then
      // embedded → just switch panes back to restaurant
      parentController.switchToRestaurantView()
    else if currentPost != null then
      // non-embedded → reuse this same stage (no new modal window)
      stage match
        case Some(s) => MainApp.replaceWithRestaurantForProduct(currentPost, s)
        case None    => MainApp.showRestaurantForProduct(currentPost) // fallback

  def show(post: ProductPost, embedded: Boolean = false, parent: RestaurantPostController = null): Unit =
    this.post = post
    setEmbeddedMode(embedded, parent)
    loadView[ProductPostController]("/byehunger/view/ProductPost.fxml").foreach(_.post = post)

  private def findAndShowParentRestaurant(): Unit =
    if currentPost != null then
      val restaurantId = Option(currentPost.restaurantPostID.value).getOrElse("")
      val parentRestaurant = MainApp.restaurantPost.find(r =>
        Option(r.restaurantPostID.value).contains(restaurantId)
      )

      parentRestaurant match
        case Some(restaurant) =>
          // Close current ProductOverview window
          if stage.isDefined then
            stage.get.close()

          // Show the parent restaurant
          MainApp.showRestaurantPostPopup(restaurant)
        case None =>
          // If no parent found, just close the window
          if stage.isDefined then
            stage.get.close()

  def loadView[T](fxmlPath: String): Option[T] =
    try {
      val resource = getClass.getResource(fxmlPath)
      val loader = new FXMLLoader(resource)
      //      loader.load()          //below call loader.load[Pane] already, dont repeat
      val newContent = loader.load[Pane]() //ScrollPane cannot, StackPane,AnchorPane,Pane can
      // Update the center content of the main window
      window.setCenter(newContent)
      Some(loader.getController[T]())
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println(s"Failed to load view: $fxmlPath")
        None
    }

  def handleProduct(): Unit =
    //    loadView("/byehunger/view/Activity.fxml")
    loadView[ProductPostController]("/byehunger/view/ProductPost.fxml") match {
      case Some(ctrl) =>
        this.productPostController = Some(ctrl)
      case None =>
        println("Failed to load ProductPost.fxml")
    }

  private def refreshRelatedProducts(): Unit =
    if currentPost == null then
      relatedProducts = Seq.empty
    else
      val currentRestaurantId = Option(currentPost.restaurantPostID.value).getOrElse("")
      // all products in same restaurant, excluding the current one, sorted by name
      relatedProducts =
        MainApp.productPost.view
          .filter(p => Option(p.restaurantPostID.value).contains(currentRestaurantId))
          .filterNot(_.productID.value == currentPost.productID.value)
          .toSeq
          .sortBy(_.productName.value)

    stripStartIndex = 0
    refreshRelatedStrip()

  private def refreshRelatedStrip(): Unit =
    if otherProductStrip == null then return
    otherProductStrip.getChildren.clear()

    if relatedProducts.isEmpty then
      // nothing to show; optionally disable buttons
      if leftButton != null then leftButton.setDisable(true)
      if rightButton != null then rightButton.setDisable(true)
      return

    if leftButton != null then leftButton.setDisable(false)
    if rightButton != null then rightButton.setDisable(false)

    val n = relatedProducts.size
    val toShow = math.min(visibleCount, n)
    (0 until toShow).foreach { i =>
      val idx = (stripStartIndex + i) % n
      val prod = relatedProducts(idx)
      otherProductStrip.getChildren.add(buildCard(prod))
    }

  private def buildCard(post: ProductPost): VBox =
    val firstImageUrl = post.imageURLs.headOption.getOrElse("")
    val imageView = new ImageView(
      if firstImageUrl.trim.nonEmpty then new Image(firstImageUrl, 120, 90, true, true)
      else new Image("data:,", 120, 90, true, true)
    )
    val name = new Label(Option(post.productName.value).getOrElse(""))
    val card = new VBox(imageView, name)
    card.setSpacing(6)
    card.getStyleClass.add("post-card")
    card.setOnMouseClicked(_ => this.post = post) // switch center to clicked product
    card

  @FXML
  def handleLeft(): Unit =
    if relatedProducts.nonEmpty then
      stripStartIndex = (stripStartIndex - visibleCount % relatedProducts.size + relatedProducts.size) % relatedProducts.size
      refreshRelatedStrip()

  @FXML
  def handleRight(): Unit =
    if relatedProducts.nonEmpty then
      stripStartIndex = (stripStartIndex + visibleCount) % relatedProducts.size
      refreshRelatedStrip()

  // Add this method to set the parent controller
  def setParentController(controller: RestaurantPostController): Unit =
    parentController = controller