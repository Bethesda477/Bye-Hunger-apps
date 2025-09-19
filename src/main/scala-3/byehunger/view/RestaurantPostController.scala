package byehunger.view

import javafx.fxml.{FXML, FXMLLoader}
import byehunger.MainApp
import byehunger.MainApp.restaurantPost
import byehunger.model.{ProductPost, RestaurantPost}
import javafx.event.ActionEvent
import javafx.scene.control.{Button, Label, TableView, TextArea, TextField}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{AnchorPane, GridPane, HBox, Pane, StackPane, TilePane, VBox, FlowPane}
import javafx.stage.Stage
import scalafx.Includes.*
import scalafx.beans.binding.Bindings
import scalafx.collections.ObservableBuffer


@FXML
class RestaurantPostController():

  var stage: Option[Stage] = None

  var dialogStage: Stage = null
  var onClicked = false

  private var currentPost: RestaurantPost = _
  private var currentImageIndex: Int = 0

  def post: RestaurantPost = currentPost
  
  def post_=(p: RestaurantPost): Unit =
    currentPost = p
    bindFields()


  //  @FXML
//  private var highlightField: Label = null
  @FXML
  private var restaurantDescriptionLabel: Label = null
//  @FXML
//  private var operationDayLabel: Label = null
  @FXML
  private var restaurantNameLabel: Label = null
  @FXML
  private var imageContainer: TilePane = null
  @FXML
  private var mainImageView: ImageView = null
  @FXML
  private var thumbnailContainer: HBox = null
  @FXML
  private var addressLabel: Label = null
  @FXML
  private var leftButton: Button = null
  @FXML
  private var rightButton: Button = null
  @FXML
  private var productList: VBox = null
  @FXML
  private var contentContainer: StackPane = null
  @FXML
  private var restaurantContent: GridPane = null
  @FXML
  private var productContent: AnchorPane = null
  @FXML
  private var categoriesContainer: FlowPane = null


  private var currentRestaurantPost: RestaurantPost = _


  // initialize Table View display contents model
  def initialize(): Unit =
    println("RestaurantPostController.initialize() called")

    if restaurantNameLabel != null then
      println("restaurantNameLabel is loaded")
      restaurantNameLabel.setText("")
    else
      println("restaurantNameLabel is null")

    if productList != null then
      println("productList is loaded")
    else
      println("productList is null")

    if addressLabel != null then
      println("addressLabel is loaded")
      addressLabel.setText("")
    else
      println("restaurantNameLabel is null")

    if contentContainer != null then
      println("contentContainer is loaded")
    else
      println("contentContainer is null")

    if thumbnailContainer != null then
      println("thumbnailContainer is loaded")
      thumbnailContainer.getChildren.clear()
    else
      println("thumbnailContainer is null")

    if mainImageView != null then
      println("mainImageView is loaded")
      mainImageView.setImage(null)
    else
      println("mainImageView is null")

    currentImageIndex = 0
  // Add this method to RestaurantPostController
  def loadProductPostView(product: ProductPost): AnchorPane =
    val resource = getClass.getResource("/byehunger/view/ProductPost.fxml")
    val loader = new FXMLLoader(resource)
    val productPane = loader.load[AnchorPane]()
    val productController = loader.getController[ProductPostController]()

    // Set the product data
    productController.post = product

    // Set up navigation back to restaurant
    productController.setParentController(this)

    productPane

  // Modify the switchToProductView method
  def switchToProductView(product: ProductPost): Unit =
    if contentContainer != null then
      // Load ProductOverview content
      val resource = getClass.getResource("/byehunger/view/ProductOverview.fxml")
      val loader = new FXMLLoader(resource)
      val productPane = loader.load[Pane]()
      val productController = loader.getController[ProductOverviewController]()

      // Set up the product controller in embedded mode
      productController.show(product, embedded = true, parent = this)

      // Replace the content
      productContent.getChildren.clear()
      productContent.getChildren.add(productPane)

      // Switch visibility
      restaurantContent.setVisible(false)
      productContent.setVisible(true)

  // Add this method to switch back to restaurant view
  def switchToRestaurantView(): Unit =
    if contentContainer != null then
      restaurantContent.setVisible(true)
      productContent.setVisible(false)

  def bindFields(): Unit =
    try {
      if currentPost != null then {
        println(s"bindFields called for restaurant: ${currentPost.restaurantName.value}")

        if restaurantDescriptionLabel != null then
          restaurantDescriptionLabel.text <== currentPost.restaurantDescription
          println("Restaurant description bound")

        if restaurantNameLabel != null then
          restaurantNameLabel.text <== currentPost.restaurantName
          println("Restaurant name bound")

        if addressLabel != null then
          addressLabel.text <== currentPost.addressURL
          println("Restaurant name bound")

        // Handle images
        if mainImageView != null then
          currentImageIndex = 0
          if currentPost.imageURLs.nonEmpty then
            println(s"Loading image: ${currentPost.imageURLs(currentImageIndex)}")
            mainImageView.setImage(new Image(currentPost.imageURLs(currentImageIndex), true))
          else
            println("No images to load")

        // Handle thumbnails
        if thumbnailContainer != null then
          thumbnailContainer.getChildren.clear()
          currentPost.imageURLs.zipWithIndex.foreach { case (url, idx) =>
            val thumb = new ImageView(new Image(url, 100, 75, true, true))
            thumb.setOnMouseClicked(_ => {
              currentImageIndex = idx
              if mainImageView != null then
                mainImageView.setImage(new Image(url, true))
            })
            thumbnailContainer.getChildren.add(thumb)
          }
          println(s"Added ${currentPost.imageURLs.size} thumbnails")

        // Handle categories
        if categoriesContainer != null then
          categoriesContainer.getChildren.clear()
          currentPost.categories.foreach { category =>
            val categoryChip = new Label(category.displayName)
            categoryChip.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15; -fx-font-size: 12;")
            categoriesContainer.getChildren.add(categoryChip)
          }
          println(s"Added ${currentPost.categories.size} category chips")

        // Load product list
        loadProducts()
      } else
        println("currentPost is null in bindFields")
    } catch {
      case e: Exception =>
        println(s"Error in bindFields: ${e.getMessage}")
        e.printStackTrace()
    }

  private def loadProducts(): Unit =
    if currentPost != null && productList != null then {
      productList.getChildren.clear()

      //Find the product in this restaurant
      val restaurantProduct = MainApp.productPost.filter(p =>
        Option(p.restaurantPostID.value).contains(currentPost.restaurantPostID.value)
      )
      restaurantProduct.foreach { product =>
        val productCard = createProductCard(product)
        productList.getChildren.add(productCard)
      }

    }

  // Create a single product card
  private def createProductCard(product: ProductPost): HBox =
    val card = new HBox()
    card.setSpacing(10)
    card.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;")
    card.setId("restaurant-product-list-card")

    // Product image
    val imageView = new ImageView()
    imageView.setFitWidth(80)
    imageView.setFitHeight(60)
    imageView.setPreserveRatio(true)

    if product.imageURLs.nonEmpty then
      imageView.setImage(new Image(product.imageURLs.head, true))

    // Product info VBox
    val infoBox = new VBox()
    infoBox.setSpacing(5)

    val nameLabel = new Label()
    nameLabel.textProperty().bind(product.productName)
    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;")

    val priceLabel = new Label()
    priceLabel.textProperty().bind(Bindings.createStringBinding(
      () => s"$$${Option(product.price.value).map(_.toString).getOrElse("0.00")}",
      product.price
    ))
    priceLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;")

    // Add categories
    val categoriesBox = new FlowPane()
    categoriesBox.setHgap(5)
    categoriesBox.setVgap(2)
    product.categories.foreach { category =>
      val categoryChip = new Label(category.displayName)
      categoryChip.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 10; -fx-font-size: 10;")
      categoriesBox.getChildren.add(categoryChip)
    }

    infoBox.getChildren.addAll(nameLabel, priceLabel, categoriesBox)
    card.getChildren.addAll(imageView, infoBox)

    // Add click handler to switch to product view
    card.setOnMouseClicked(_ => {
      switchToProductView(product)
    })

    // Add hover effects
    card.setOnMouseEntered(_ => {
      card.setStyle("-fx-padding: 10; -fx-border-color: #007bff; -fx-border-radius: 5; -fx-background-radius: 5; -fx-background-color: #f8f9fa;")
    })

    card.setOnMouseExited(_ => {
      card.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;")
    })

    card


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
    if currentPost != null then MainApp.addToFavourites(currentPost)


  @FXML
  def handleNavigation(): Unit =
    if currentPost != null then MainApp.openMapFor(currentPost)


  @FXML
  def handleClose(action: ActionEvent): Unit =
    onClicked = true
    stage.foreach(x => x.close())

  def nullChecking(x: String) =
    x == null || x.length == 0




  