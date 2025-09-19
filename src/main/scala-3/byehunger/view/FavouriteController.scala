package byehunger.view

import javafx.fxml.FXML
import byehunger.MainApp
import byehunger.model.{ProductPost, RestaurantPost}
import javafx.event.ActionEvent
import javafx.scene.control.{Button, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{TilePane, VBox}
import scalafx.Includes.*

import java.net.URL
import java.nio.file.{Files, Paths}
import scala.util.Try

@FXML
class FavouriteController():

  @FXML
  private var favRestaurantTilePane: TilePane = _
  @FXML
  private var favProductTilePane: TilePane = _

  def initialize(): Unit = {
    reloadGallery()
    MainApp.favouritePost.onChange { (_, _) =>
      reloadGallery()
    }
    MainApp.favouriteProductPost.onChange{(_,_) =>
      reloadGallery()
    }
  }




  private def reloadGallery(): Unit = {
    if (favRestaurantTilePane == null) return
    favRestaurantTilePane.getChildren.clear()
    MainApp.favouritePost.foreach { post =>
      favRestaurantTilePane.getChildren.add(createPostTile(post))
    }
    if (favProductTilePane == null) return
    favProductTilePane.getChildren.clear()
    MainApp.favouriteProductPost.foreach { post =>
      favProductTilePane.getChildren.add(createProductPostTile(post))
    }
  }
  @FXML
  private def createPostTile(post: RestaurantPost): VBox =
    val firstImageUrl = post.imageURLs.headOption.getOrElse(Option(post.imageURL.value).getOrElse(""))
    val imageView = buildSafeImageView(firstImageUrl, fitWidth = 160, fitHeight = 120)


    val title = new Label()
    title.textProperty().bind(post.restaurantName)

    val openButton = new Button("Open")
    openButton.setOnAction(_ => MainApp.showRestaurantPostPopup(post))

    val removeButton = new Button("Remove")
    removeButton.setOnAction(_ => MainApp.favouritePost -= post)

    val box = new VBox(imageView, title, openButton, removeButton)
    box.setSpacing(8)
    box.getStyleClass.add("post-card")
    box

  @FXML
  private def createProductPostTile(post: ProductPost): VBox =
    val firstImageUrl = post.imageURLs.headOption.getOrElse("")
    val imageView = buildSafeImageView(firstImageUrl, fitWidth = 160, fitHeight = 120)


    val title = new Label()
    title.textProperty().bind(post.productName)

    val openButton = new Button("Open")
    openButton.setOnAction(_ => MainApp.showProductOverview(post))

    val removeButton = new Button("Remove")
    removeButton.setOnAction(_ => MainApp.favouriteProductPost -= post)

    val box = new VBox(imageView, title, openButton, removeButton)
    box.setSpacing(8)
    box.getStyleClass.add("post-card")
    box

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


