package byehunger.view

import byehunger.MainApp
import byehunger.model.ProductPost
import javafx.fxml.FXML
import javafx.scene.control.{Label, Button}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{TilePane, VBox}

import scalafx.Includes.*

class FoodController:

  @FXML
  private var productTilePane: TilePane = _

  @FXML
  def initialize(): Unit =
    println("FoodController initialized successfully")
    showProductGallery()

  def showProductGallery(): Unit =
    productTilePane.getChildren.clear()
    val fromDb = ProductPost.getAllProductPosts
    fromDb.foreach { post => 
      productTilePane.getChildren.add(createProductPostTile(post))
    }

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


