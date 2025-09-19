package byehunger.view

import javafx.fxml.{FXML, FXMLLoader}
import byehunger.MainApp
import byehunger.model.{ProductPost, RestaurantPost}
import byehunger.util.DateUtil
import javafx.event.ActionEvent
import javafx.scene.control.{Button, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{BorderPane, Pane, StackPane, TilePane, VBox}
import scalafx.Includes.*
import scalafx.scene.control.{Alert, ButtonType, TableRow}
import javafx.beans.binding.Bindings as JfxBindings

import java.net.URL
import java.nio.file.{Files, Paths}
import java.text.Normalizer
import scala.util.Try

@FXML
class MainWindowController():

  var myPostController: Option[MyPostController] = None
  var nearbyController: Option[NearbyController] = None
  var favouriteController: Option[FavouriteController] = None
  var activityController: Option[ActivityController] = None


  @FXML
  private var searchBarText: javafx.scene.control.TextField = null

  @FXML
  private var mainWindow: BorderPane = _
  
  @FXML
  private var productTilePane: TilePane = _
  
  @FXML
  private var restaurantTilePane: TilePane = _



  def initialize(): Unit =

    // Always refresh from DB when opening the MyPost view
    try
      val fromDb = RestaurantPost.getAllRestaurantPosts
      val fromProductDb = ProductPost.getAllProductPosts
      MainApp.restaurantPost.clear()
      MainApp.restaurantPost ++= fromDb
      MainApp.productPost.clear()
      MainApp.productPost ++= fromProductDb
      println(s"[UI] Loaded ${fromDb.size} posts from DB into MyPost view.")
      println(s"[UI] Loaded ${fromProductDb.size} posts from DB into MyPost view.")
    catch
      case ex: Throwable =>
        ex.printStackTrace()
        new Alert(Alert.AlertType.Error):
          initOwner(MainApp.stage)
          title = "Load failed"
          headerText = "Could not load posts from the database"
          contentText = Option(ex.getMessage).getOrElse(ex.toString)
        .showAndWait()

    // initial render
    showRestaurantGallery(restaurantTilePane)
    showProductGallery(productTilePane)
  
    val applySearch = () => {
      val q = Option(searchBarText.getText).getOrElse("")
      showRestaurantGallery(restaurantTilePane, q)
      showProductGallery(productTilePane, q)
    }
  
    // live filter on typing
    searchBarText.textProperty().addListener((_, _, _) => applySearch())
  
    // keep search results in sync with data changes
    MainApp.productPost.onChange { (_, _) => applySearch() }
    MainApp.restaurantPost.onChange { (_, _) => applySearch() }

//    showRestaurantGallery(restaurantTilePane)
//    showProductGallery(productTilePane)
//    // Refresh gallery automatically when the list changes
//    MainApp.productPost.onChange { (_, _) =>
//      showProductGallery(productTilePane)
//    }
//    MainApp.restaurantPost.onChange { (_, _) =>
//      showRestaurantGallery(restaurantTilePane)
//    }


  @FXML
  def handlePopup(action: ActionEvent): Unit =
    val source = action.getSource
    source match {
      case button: Button =>
        button.id.value match {
//          case "messageIcon" => MainApp.showPopup("/byehunger/view/MessageIcon.fxml", button)
          case "profileIcon" => MainApp.showPopup("/byehunger/view/ProfileIcon.fxml", button)
          case "cartButton" => MainApp.showPopup("/byehunger/view/CartIcon.fxml", button)
          case _ => // Do nothing for unknown button IDs
        }
      case imageView: ImageView =>
        imageView.id.value match {
          case "restaurantPost" => MainApp.showPopup("/byehunger/view/RestaurantPost.fxml", imageView)
          case "productPost" => MainApp.showPopup("/byehunger/view/ProductPost.fxml", imageView)
          case "activityPost" => MainApp.showPopup("/byehunger/view/ActivityPost.fxml", imageView)
          case _ => // Do nothing for unknown ImageView IDs
        }
      case _ => // Do nothing for unknown source types
    }




  def loadView[T](fxmlPath: String): Option[T] =
    try {
      val resource = getClass.getResource(fxmlPath)
      val loader = new FXMLLoader(resource)
//      loader.load()          //below call loader.load[Pane] already, dont repeat
      val newContent = loader.load[Pane]() //ScrollPane cannot, StackPane,AnchorPane,Pane can
      // Update the center content of the main window
      mainWindow.setCenter(newContent)
      Some(loader.getController[T]())
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println(s"Failed to load view: $fxmlPath")
        None
    }






  def handleHome(): Unit = {
    //show back the MainWindow instead of a new Home.fxml
    MainApp.showMainWindow()
  }

  def handleActivity(): Unit =
//    loadView("/byehunger/view/Activity.fxml")
    loadView[ActivityController]("/byehunger/view/Activity.fxml") match {
      case Some(ctrl) =>
        this.activityController = Some(ctrl)
      case None =>
        println("Failed to load Activity.fxml")
    }

  def handleNearby(): Unit =
    loadView[NearbyController]("/byehunger/view/Nearby.fxml") match {
      case Some(ctrl) =>
        this.nearbyController = Some(ctrl)
      case None =>
        println("Failed to load Nearby.fxml")
    }

  def handleMyPost(): Unit =
    loadView[MyPostController]("/byehunger/view/MyPost.fxml") match {
      case Some(ctrl) =>
        this.myPostController = Some(ctrl)
      case None =>
        println("Failed to load MyPost.fxml")
    }
//    val myPostController = loader.getController[MyPostController]()

  def handleFavourite(): Unit =
//    loadView("/byehunger/view/Favourite.fxml")
    loadView[FavouriteController]("/byehunger/view/Favourite.fxml") match {
      case Some(ctrl) =>
        this.favouriteController = Some(ctrl)
      case None =>
        println("Failed to load Favourite.fxml")
    }

//  def handleCartIcon(): Unit =



  def handleLogout(): Unit = {
    val result =
      new Alert(Alert.AlertType.Confirmation):
        initOwner(MainApp.stage)
        title = "Log Out"
        headerText = "Are you sure you want to log out?"
        contentText = "You will need to log in again to access the system."
        buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
      .showAndWait()
      
    result match {
      case Some(ButtonType.OK) =>
        println("User confirmed logout")
        MainApp.showLogin()
      case _=>
        println("User canceled logout")
    }
    
    
    
  }

  @FXML
  def handleRestaurant(): Unit =
    loadView[RestaurantController]("/byehunger/view/Restaurant.fxml") match {
      case Some(ctrl) =>
        // Restaurant view loaded successfully
        println("Restaurant view loaded successfully")
      case None =>
        println("Failed to load Restaurant.fxml")
    }

  @FXML
  def handleFood(): Unit =
    loadView[FoodController]("/byehunger/view/Food.fxml") match {
      case Some(ctrl) =>
        // Food view loaded successfully
        println("Food view loaded successfully")
      case None =>
        println("Failed to load Food.fxml")
    }


  @FXML
  def handleSectionChange(action: ActionEvent): Unit =
    val button = action.getSource.asInstanceOf[Button]
    button.getId match
      case "homeButton" => handleHome()
      case "activityButton" => handleActivity()
      case "nearbyButton" => handleNearby()
      case "myPostButton" => handleMyPost()
      case "favouriteButton" => handleFavourite()
      case "logoutButton" => handleLogout()
      case "restaurantButton" => handleRestaurant()
      case "foodButton" => handleFood()

      // Add more cases for other sections
      case _ => println(s"Unknown button ID: ${button.getId}")

//  def showRestaurantGallery(tilePane: TilePane): Unit =
//    restaurantTilePane.getChildren.clear()
//    MainApp.restaurantPost.foreach { post =>
//      restaurantTilePane.getChildren.add(createPostTile(post))
//    }

  /** Create a single post tile with image, restaurant name, date, and short description */
  private def createPostTile(post: RestaurantPost): VBox =
    val firstImageUrl = post.imageURLs.headOption.getOrElse("")
    val imgView = buildSafeImageView(firstImageUrl, fitWidth = 160, fitHeight = 120)

    // Bind directly to the RestaurantPost properties so the labels reflect current data
    val restaurantNameLabel = new Label()
    restaurantNameLabel.textProperty().unbind()
    restaurantNameLabel.textProperty().bind(post.restaurantName)

    val dateLabel = new Label()
    val dateBinding = JfxBindings.createStringBinding(
      () => Option(post.dateOfCreated.value).map(DateUtil.DATE_FORMATTER.format).getOrElse(""),
      post.dateOfCreated
    )
    dateLabel.textProperty().bind(dateBinding)
    dateLabel.getStyleClass.add("post-date")

//    val descLabel = new Label(
//      Option(post.restaurantDescription.value)
//        .map(_.trim)
//        .filter(_.nonEmpty)
//        .map(t => if t.length <= 50 then t else t.take(50) + "...")
//        .getOrElse("")
//    )
//    descLabel.getStyleClass.add("post-description")
//    descLabel.setWrapText(true)

    val navButton = new Button("Navigate🗺")
    navButton.setId("navigateButton")
    navButton.setOnAction(_ => MainApp.openMapFor(post))
    navButton.disableProperty().bind(
      post.addressURL.isEmpty.and(restaurantNameLabel.textProperty().isEmpty)
    )
    navButton.setStyle("-fx-background-color: #2651e0; -fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-padding: 8 16;")

    val favouriteButton = new Button("Favourite💓")
    favouriteButton.setOnAction(_ => MainApp.addToFavourites(post))
    favouriteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-padding: 8 16;")


    val card = new VBox(imgView, restaurantNameLabel, dateLabel, navButton, favouriteButton)
    card.setSpacing(8)
    card.getStyleClass.add("post-card")


    // Click to open popup
    card.setOnMouseClicked(_ => MainApp.showRestaurantPostPopup(post))

    card

  def showRestaurantGallery(tilePane: TilePane, query: String = ""): Unit =
    restaurantTilePane.getChildren.clear()
    val items = MainApp.restaurantPost.filter(r => nameMatches(query, r.restaurantName.value))
    items.foreach { post => restaurantTilePane.getChildren.add(createPostTile(post)) }

  def showProductGallery(tilePane: TilePane, query: String = ""): Unit =
    productTilePane.getChildren.clear()
    val items = MainApp.productPost.filter(p => nameMatches(query, p.productName.value))
    items.foreach { post => productTilePane.getChildren.add(createProductPostTile(post)) }
//  def showProductGallery(tilePane: TilePane): Unit = {
//    productTilePane.getChildren.clear()
//    MainApp.productPost.foreach{post =>
//      productTilePane.getChildren.add(createProductPostTile(post))
//    }
//  }

  def createProductPostTile(post: ProductPost): VBox =
    val firstImageUrl = post.imageURLs.headOption.getOrElse("")
    val imageView = buildSafeImageView(firstImageUrl, fitWidth = 160, fitHeight = 120)


    val title = new Label()
    title.textProperty().bind(post.productName)

    val price = new Label()
    price.textProperty().bind(post.price.asString("RM%.2f"))

    val favouriteButton = new Button("Favourite💓")
    favouriteButton.setOnAction(_ => MainApp.addProductToFavourites(post))
    favouriteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: #ffffff; -fx-background-radius: 8; -fx-padding: 8 16;")
    val addToCartButton = new Button("Cart🛒")
    addToCartButton.setOnAction(_ => MainApp.addToCart(post))
    addToCartButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8 16; -fx-font-weight: bold;")
    
    val card = new VBox(imageView, title, price, favouriteButton, addToCartButton)
    card.setSpacing(8)

    card.getStyleClass.add("post-card")
    //click to open
    card.setOnMouseClicked((_ => MainApp.showProductOverview(post)))

    card



  private def buildSafeImageView(raw: String, fitWidth: Double, fitHeight: Double): ImageView =
    val iv = new ImageView()
    iv.setFitWidth(fitWidth)
    iv.setFitHeight(fitHeight)
    iv.setPreserveRatio(true)

    val sourceUrl = resolveImageUrl(raw).getOrElse(defaultPlaceholderUrl)
    iv.setImage(new Image(sourceUrl, true))

    // If background loading fails, swap to placeholder to avoid runtime errors.
    iv.imageProperty().addListener((_, _, newImg) => {
      if newImg != null && newImg.isError then
        iv.setImage(new Image(defaultPlaceholderUrl, true))
    })

    iv

    /** Resolve a raw string into a URL string acceptable by JavaFX Image. */
  private def resolveImageUrl(raw: String): Option[String] =
    if raw == null || raw.trim.isEmpty then return None
    val trimmed = raw.trim

    // 1) Already a well-formed URL? (http/https/file/jar etc.)
    if isWellFormedUrl(trimmed) then return Some(trimmed)

    // 2) Classpath resource? Try as-is and with leading slash.
    val cp1 = Option(getClass.getResource(trimmed))
    val cp2 = cp1.orElse(Option(getClass.getResource("/" + trimmed)))
    cp2.map(_.toExternalForm) match
      case some@Some(_) => return some
      case None => ()

    // 3) Filesystem path? Convert to file: URI if it exists.
    val path = Paths.get(trimmed)
    if Files.exists(path) then
      return Some(path.toUri.toString)

    None

  private def isWellFormedUrl(s: String): Boolean =
    Try(new URL(s)).isSuccess

  // Points to src/main/resources/images/placeholder.png at runtime
  private lazy val defaultPlaceholderUrl: String =
    Option(getClass.getResource("/images/placeholder.png"))
      .map(_.toExternalForm)
      .getOrElse("data:,") // harmless empty data URL as last resort


  private def normalize(s: String): String =
    if s == null then ""
    else Normalizer
      .normalize(s.toLowerCase(java.util.Locale.ROOT).trim, Normalizer.Form.NFD)
      .replaceAll("\\p{M}+", "")

  private def nameMatches(query: String, candidate: String): Boolean =
    val q = normalize(query)
    if q.isEmpty then true else normalize(candidate).contains(q)