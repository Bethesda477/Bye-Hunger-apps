package byehunger.view

import javafx.fxml.{FXML, FXMLLoader}
import byehunger.MainApp
import byehunger.model.{ProductPost, RestaurantPost}
import byehunger.util.DateUtil
import byehunger.util.MapNavigator
import javafx.event.ActionEvent
import javafx.scene.control.{Button, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{BorderPane, Pane, StackPane, TilePane, VBox}
import scalafx.Includes.*
import scalafx.scene.control.{Alert, ButtonType, TableRow}
import javafx.beans.binding.Bindings as JfxBindings
import javafx.scene.chart.{BarChart, XYChart}

import java.net.URL
import java.nio.file.{Files, Paths}
import scala.util.Try

@FXML
class MerchantMainWindowController():

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


  @FXML private var revenueChart: BarChart[String, Number] = _
  @FXML private var favouriteChart: BarChart[String, Number] = _
  @FXML private var navigationChart: BarChart[String, Number] = _
  @FXML private var viewsChart: BarChart[String, Number] = _

  @FXML
  def initialize(): Unit =

    populateAll()
    // Refresh when lists change
    MainApp.restaurantPost.onChange { (_, _) => populateAll() }
    MainApp.productPost.onChange { (_, _) => populateAll() }
    //     Always refresh from DB when opening the MyPost view
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

  //    showRestaurantGallery(restaurantTilePane)
  //    showProductGallery(productTilePane)
  //    // Refresh gallery automatically when the list changes
  //    MainApp.productPost.onChange { (_, _) =>
  //      showProductGallery(productTilePane)
  //    }
  //    MainApp.restaurantPost.onChange { (_, _) =>
  //      showRestaurantGallery(restaurantTilePane)
  //    }

  private def populateAll(): Unit =
    populateRevenue()
    populateFavourite()
    populateNavigation()
    populateViews()

  private def populateRevenue(): Unit =
    revenueChart.getData.clear()
    val s = new XYChart.Series[String, Number]()
    s.setName("Total Revenue")
    // Mock revenue: sum of prices of products under each restaurant
    MainApp.restaurantPost.foreach { r =>
      val total = r.products.view.map(_.price.value).sum
      s.getData.add(new XYChart.Data(r.restaurantName.value, total))
    }
    revenueChart.getData.add(s)

  private def populateFavourite(): Unit =
    favouriteChart.getData.clear()
    val s = new XYChart.Series[String, Number]()
    s.setName("Favourite Clicks")
    val clicks = MainApp.favouriteClicksByRestaurant
    MainApp.restaurantPost.foreach { r =>
      val key = Option(r.restaurantPostID.value).getOrElse("")
      val count = clicks.getOrElse(key, 0)
      s.getData.add(new XYChart.Data(r.restaurantName.value, count))
    }
    favouriteChart.getData.add(s)

  private def populateNavigation(): Unit =
    navigationChart.getData.clear()
    val s = new XYChart.Series[String, Number]()
    s.setName("Navigation Clicks")
    val clicks = MainApp.navigationClicksByRestaurant
    MainApp.restaurantPost.foreach { r =>
      val key = Option(r.restaurantPostID.value).getOrElse("")
      val count = clicks.getOrElse(key, 0)
      s.getData.add(new XYChart.Data(r.restaurantName.value, count))
    }
    navigationChart.getData.add(s)

  private def populateViews(): Unit =
    viewsChart.getData.clear()
    val s = new XYChart.Series[String, Number]()
    s.setName("Mock Post Views")
    // Mock views: number of products per restaurant * 10
    MainApp.restaurantPost.foreach { r =>
      val views = r.products.size * 10
      s.getData.add(new XYChart.Data(r.restaurantName.value, views))
    }
    viewsChart.getData.add(s)
  

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
    MainApp.showMerchantMainWindow()
  }



  def handleMyPost(): Unit =
    loadView[MyPostController]("/byehunger/view/MyPost.fxml") match {
      case Some(ctrl) =>
        this.myPostController = Some(ctrl)
      case None =>
        println("Failed to load MyPost.fxml")
    }
//    val myPostController = loader.getController[MyPostController]()

  

  @FXML
  def handleSectionChange(action: ActionEvent): Unit =
    val button = action.getSource.asInstanceOf[Button]
    button.getId match
      case "homeButton" => handleHome()
      case "myPostButton" => handleMyPost()
//      case "favouriteButton" => handleFavourite()
      case "logoutButton" => handleLogout()

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

    val navButton = new Button("Navigate")
    navButton.setId("navigateButton")
    navButton.setOnAction(_ => MainApp.openMapFor(post))
    navButton.disableProperty().bind(
      post.addressURL.isEmpty.and(restaurantNameLabel.textProperty().isEmpty)
    )

    val favouriteButton = new Button("Favourite")
    favouriteButton.setOnAction(_ => MainApp.addToFavourites(post))


    val card = new VBox(imgView, restaurantNameLabel, dateLabel, navButton, favouriteButton)
    card.setSpacing(8)
    card.getStyleClass.add("post-card")


    // Click to open popup
    card.setOnMouseClicked(_ => MainApp.showRestaurantPostPopup(post))

    card

  def showProductGallery(tilePane: TilePane): Unit = {
    productTilePane.getChildren.clear()
    MainApp.productPost.foreach{post =>
      productTilePane.getChildren.add(createProductPostTile(post))
    }
  }

  def createProductPostTile(post: ProductPost): VBox =
    val firstImageUrl = post.imageURLs.headOption.getOrElse("")
    val imageView = buildSafeImageView(firstImageUrl, fitWidth = 160, fitHeight = 120)


    val title = new Label()
    title.textProperty().bind(post.productName)

    val price = new Label()
    price.textProperty().bind(post.price.asString("RM%.2f"))

    val favouriteButton = new Button("Favourite")
    favouriteButton.setOnAction(_ => MainApp.addProductToFavourites(post))

    val addToCartButton = new Button("Add to Cart")
    addToCartButton.setOnAction(_ => MainApp.addToCart(post))
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
        MainApp.showMerchantLogin()
      case _ =>
        println("User canceled logout")
    }


  }