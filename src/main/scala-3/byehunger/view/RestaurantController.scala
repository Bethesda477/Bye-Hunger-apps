package byehunger.view

import byehunger.MainApp
import byehunger.model.RestaurantPost
import javafx.fxml.FXML
import javafx.scene.control.{Label, Button}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{TilePane, VBox}
import javafx.beans.binding.Bindings as JfxBindings
import byehunger.util.DateUtil

import scalafx.Includes.*

class RestaurantController:

  @FXML
  private var restaurantTilePane: TilePane = _

  @FXML
  def initialize(): Unit =
    println("RestaurantController initialized successfully")
    showRestaurantGallery()

  def showRestaurantGallery(): Unit =
    restaurantTilePane.getChildren.clear()
    val fromDb = RestaurantPost.getAllRestaurantPosts
    fromDb.foreach { post => 
      restaurantTilePane.getChildren.add(createPostTile(post))
    }

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
    val path = java.nio.file.Paths.get(trimmed)
    if java.nio.file.Files.exists(path) then
      return Some(path.toUri.toString)

    None

  private def isWellFormedUrl(s: String): Boolean =
    scala.util.Try(new java.net.URL(s)).isSuccess

  // Points to src/main/resources/images/placeholder.png at runtime
  private lazy val defaultPlaceholderUrl: String =
    Option(getClass.getResource("/images/placeholder.png"))
      .map(_.toExternalForm)
      .getOrElse("data:,") // harmless empty data URL as last resort


