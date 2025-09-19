package byehunger.view

import javafx.fxml.{FXML, FXMLLoader}
import byehunger.MainApp
import byehunger.model.{ProductPost, RestaurantPost}
import byehunger.util.{DateUtil, MapNavigator, SoundEffect}
import javafx.scene.control.{Button, Label, TableColumn, TableRow, TableView}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{TilePane, VBox}
import scalafx.scene.control.Alert
import scalafx.Includes.*
import scalafx.beans.binding.Bindings
import scalafx.beans.value.ObservableValue
import scalafx.scene.Scene
import scalafx.stage.{Modality, Stage}

import scala.util.{Failure, Success}
import java.net.{URL, URLEncoder}
import java.nio.file.{Files, Paths}
import scala.util.Try
import javafx.beans.binding.Bindings as JfxBindings
import javafx.event.ActionEvent

import java.nio.charset.StandardCharsets // add this for JavaFX string binding

class MyPostController():

  // TableView
  @FXML private var postTable: TableView[RestaurantPost] = null
  @FXML private var dateOfCreatedColumn: TableColumn[RestaurantPost, String] = null
  @FXML private var nameColumn: TableColumn[RestaurantPost, String] = null
  @FXML private var postIDColumn: TableColumn[RestaurantPost, String] = null

  //Product TableView
  @FXML private var productTable: TableView[ProductPost] = null
  @FXML private var productIDColumn: TableColumn[ProductPost, String] = null
  @FXML private var productNameColumn: TableColumn[ProductPost, String] = null
  @FXML private var productDateColumn: TableColumn[ProductPost, String] = null
  @FXML private var productRestaurantColumn: TableColumn[ProductPost, String] = null

  // Gallery TilePane
  @FXML private var postGallery: TilePane = _

  def table: TableView[RestaurantPost] = postTable

  def initialize(): Unit =

    setRestaurantTable()
    setProductTable()
    doubleClickTable()

    // Load initial gallery
    loadGallery()

    // Refresh gallery automatically when the list changes
    MainApp.restaurantPost.onChange { (_, _) =>
      loadGallery()
    }

  /** Add new post */
  @FXML
  def handleAdd(): Unit =
    MainApp.showForm(None) // add mode

  /** Edit selected post */
  @FXML
  def handleEdit(): Unit =
    val selected = postTable.getSelectionModel.getSelectedItem
    if selected != null then
      MainApp.showForm(Some(selected))
    else
      SoundEffect.playAlertAudio()
      new Alert(Alert.AlertType.Warning):
        initOwner(MainApp.stage)
        title = "No Selection"
        headerText = "No Post Selected"
        contentText = "Please select a post in the table before editing."
      .showAndWait()

  /** Delete selected post */
  @FXML
  def handleDelete(): Unit =
    val selected = postTable.getSelectionModel.getSelectedItem
    if selected != null then {
      selected.delete() match {
        case Success(_) =>
          MainApp.restaurantPost -= selected
        case Failure(ex) =>
          ex.printStackTrace()
          SoundEffect.playAlertAudio()
          new Alert(Alert.AlertType.Error):
            initOwner(MainApp.stage)
            title = "Delete failed"
            headerText = "Could not delete the post"
            contentText = Option(ex.getMessage).getOrElse(ex.toString)
          .showAndWait()
      }
    } else
      SoundEffect.playAlertAudio()
      new Alert(Alert.AlertType.Warning):
        initOwner(MainApp.stage)
        title = "No Selection"
        headerText = "No Post Selected"
        contentText = "Please select a post in the table before deleting."
      .showAndWait()

  @FXML
  def handleAddProduct(): Unit =
    val selected: RestaurantPost = postTable.getSelectionModel.getSelectedItem
    if selected != null then
      MainApp.showProductForm(selected)
    else
      SoundEffect.playAlertAudio()
      new Alert(Alert.AlertType.Warning):
        initOwner(MainApp.stage)
        title = "No Restaurant Post Selection"
        headerText = "No Restaurant Post Selected"
        contentText = "Please select a restaurant post in the table before editing."
      .showAndWait()

  @FXML
  def handleEditProduct(): Unit =
    val selected = productTable.getSelectionModel.getSelectedItem
    if selected != null then
      // Find the parent restaurant for this product
      val parentRestaurant = MainApp.restaurantPost.find(r =>
        Option(r.restaurantPostID.value).contains(selected.restaurantPostID.value)
      )

      parentRestaurant match
        case Some(restaurant) =>
          MainApp.showProductForm(restaurant, Some(selected))
        case None =>
          new Alert(Alert.AlertType.Error):
            initOwner(MainApp.stage)
            title = "Error"
            headerText = "Parent restaurant not found"
            contentText = "Could not find the parent restaurant for this product."
          .showAndWait()
    else
      SoundEffect.playAlertAudio()
      new Alert(Alert.AlertType.Warning):
        initOwner(MainApp.stage)
        title = "No Selection"
        headerText = "No Product Selected"
        contentText = "Please select a product in the table before editing."
      .showAndWait()

  @FXML
  def handleDeleteProduct(): Unit =
    val selected = productTable.getSelectionModel.getSelectedItem
    if selected != null then {
      selected.delete() match {
        case Success(_) =>
          MainApp.productPost -= selected
        case Failure(ex) =>
          ex.printStackTrace()
      }
    }
    else
      SoundEffect.playAlertAudio()
      new Alert(Alert.AlertType.Warning):
        initOwner(MainApp.stage)
        title = "No Selection"
        headerText = "No Post Selected"
        contentText = "Please select a post in the product table before deleting."
      .showAndWait()


  private def setProductTable(): Unit =
    // Product table bindings
    productIDColumn.setCellValueFactory(cd => cd.getValue.productID)
    productNameColumn.setCellValueFactory(cd => cd.getValue.productName)
    productDateColumn.setCellValueFactory { cd =>
      val p = cd.getValue.date
      Bindings.createStringBinding(
        () => Option(p.value).map(DateUtil.DATE_FORMATTER.format).getOrElse(""),
        p
      ).asInstanceOf[ObservableValue[String, String]]
    }
    // Lookup restaurant name by restaurantPostID
    productRestaurantColumn.setCellValueFactory { cd =>
      val ridProp = cd.getValue.restaurantPostID
      Bindings.createStringBinding(
        () => {
          val rid = Option(ridProp.value).getOrElse("")
          MainApp.restaurantPost
            .find(r => Option(r.restaurantPostID.value).contains(rid))
            .map(_.restaurantName.value)
            .getOrElse("")
        },
        ridProp
      ).asInstanceOf[ObservableValue[String, String]]
    }

    // Set data
    productTable.setItems(MainApp.productPost)

  private def setRestaurantTable(): Unit =
    // Bind Table Column
    dateOfCreatedColumn.setCellValueFactory { cd =>
      val p = cd.getValue.dateOfCreated
      Bindings.createStringBinding(
        () => Option(p.value).map(DateUtil.DATE_FORMATTER.format).getOrElse(""),
        p
      ).asInstanceOf[ObservableValue[String, String]]
    }
    nameColumn.setCellValueFactory(cellData =>
      cellData.getValue.restaurantName
    )
    postIDColumn.setCellValueFactory(cellData =>
      cellData.getValue.restaurantPostID
    )

    // Load Table Data
    postTable.setItems(MainApp.restaurantPost)

  private def doubleClickTable(): Unit = {
    // Double-click handler on table rows
    postTable.setRowFactory(_ => {
      val row = new TableRow[RestaurantPost]()
      row.setOnMouseClicked(event => {
        if (event.getClickCount == 2 && !row.isEmpty) {
          SoundEffect.playOpenPostAudio()
          MainApp.showRestaurantPostPopup(row.getItem)
        }
      })
      row
    })
    productTable.setRowFactory(_ => {
      val productRow = new TableRow[ProductPost]()
      productRow.setOnMouseClicked(event => {
        if (event.getClickCount == 2 & !productRow.isEmpty) {
          SoundEffect.playOpenPostAudio()
          MainApp.showProductOverview(productRow.getItem)
        }
      })
      productRow
    })
  }
  /** Load all posts into the TilePane gallery */
  def loadGallery(): Unit =
    postGallery.getChildren.clear()
    MainApp.restaurantPost.foreach { post =>
      postGallery.getChildren.add(createPostTile(post))
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

//    val descLabel = new Label(
//      Option(post.restaurantDescription.value)
//        .map(_.trim)
//        .filter(_.nonEmpty)
//        .map(t => if t.length <= 50 then t else t.take(50) + "...")
//        .getOrElse("")
//    )
//    descLabel.getStyleClass.add("post-description")
//    descLabel.setWrapText(true)

//    val navButton = new Button("Navigate")
//    navButton.setOnAction(_ => MainApp.openMapFor(post))
//    navButton.disableProperty().bind(
//      post.addressURL.isEmpty.and(restaurantNameLabel.textProperty().isEmpty)
//    )
//    
//    val favouriteButton = new Button("Favourite")
//    favouriteButton.setOnAction(_=> MainApp.addToFavourites(post))
    
    



    val card = new VBox(imgView, restaurantNameLabel, dateLabel)
    card.setSpacing(8)
    card.getStyleClass.add("post-card")


    // Click to open popup
    card.setOnMouseClicked(_ =>
      MainApp.showRestaurantPostPopup(post)
      SoundEffect.playOpenPostAudio()
    )

    card
  

  /** Build an ImageView safely from a raw string (URL, classpath path, or filesystem path). */
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
      case some @ Some(_) => return some
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