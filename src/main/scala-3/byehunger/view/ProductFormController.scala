package byehunger.view

import byehunger.MainApp
import javafx.scene.control.{Button, Label, TextArea, TextField, CheckBox}
import javafx.stage.{FileChooser, Stage}
import byehunger.model.{ProductPost, RestaurantPost, Category}
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.scene.image.ImageView
import javafx.scene.layout.{HBox, VBox}
import javafx.util.converter.{DoubleStringConverter, NumberStringConverter}
import scalafx.collections.ObservableBuffer
import scalikejdbc.*
import scalafx.Includes.*
import scalafx.scene.control.Alert

import java.io.File

class ProductFormController() extends ImageManagement:

  @FXML
  private var productNameField: TextField = _
  @FXML
  private var productDescriptionField: TextArea = _
  @FXML
  private var imagePreviewPane: HBox = _
  @FXML
  private var priceField: TextField = _
  @FXML
  private var okButton: Button = _
  @FXML
  private var cancelButton: Button = _
  @FXML
  private var addImageButton: Button = _
  @FXML
  private var placeholderLabel: Label = _
  @FXML
  private var categoriesContainer: javafx.scene.layout.FlowPane = _

  var stage: Option[Stage] = None
  var restaurant: Option[RestaurantPost] = None // set this before showing
  var imageUrls: ObservableBuffer[String] = ObservableBuffer.empty[String]
  var selectedCategories: ObservableBuffer[Category] = ObservableBuffer.empty[Category]
  private var editing: Boolean = false
  private var currentPost: ProductPost = null
  
  // Override maxImages to allow 4 images for products
  override protected val maxImages: Int = 4
  
  // Implement abstract methods required by ImageManagement trait
  override def getImageUrls: ObservableBuffer[String] = this.imageUrls
  override def getStage: Option[Stage] = this.stage
  override def getImagePreviewPane: HBox = this.imagePreviewPane

  @FXML
  def initialize(): Unit =
    // Setup drag and drop functionality
    setupDragAndDrop()
    
    // Initialize category checkboxes
    initializeCategoryCheckboxes()





  /** Initialize category checkboxes in horizontal flow layout */
  private def initializeCategoryCheckboxes(): Unit =
    if categoriesContainer != null then
      categoriesContainer.getChildren.clear()
      
      // Configure FlowPane for horizontal flow with wrapping
      categoriesContainer.setHgap(10.0)  // Horizontal gap between items
      categoriesContainer.setVgap(5.0)   // Vertical gap between rows
      categoriesContainer.setPrefWrapLength(400.0)  // Width before wrapping
      
      // Create a label
      val label = new Label("Categories (max 3):")
      label.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 0 0 5 0;")
      categoriesContainer.getChildren.add(label)
      
      // Create checkboxes for each category in a horizontal flow
      Category.values.foreach { category =>
        val checkbox = new CheckBox(category.displayName)
        checkbox.setUserData(category)
        checkbox.setStyle("-fx-padding: 5 8; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 3;")
        
        checkbox.setOnAction(_ => {
          if checkbox.isSelected then
            if selectedCategories.size < 3 then
              selectedCategories += category
              checkbox.setStyle("-fx-padding: 5 8; -fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-radius: 3;")
            else
              checkbox.setSelected(false)
              new Alert(Alert.AlertType.Warning):
                initOwner(stage.orNull)
                title = "Categories Restriction"
                headerText = "You can only choose up to 3 types of categories"
                contentText = "Please unselect other before you continue to choose. "
              .showAndWait()
          else
            selectedCategories -= category
            checkbox.setStyle("-fx-padding: 5 8; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 3;")
        })
        
        categoriesContainer.getChildren.add(checkbox)
      }

  /** Override refreshImagePreview to handle placeholder and add button */
  override def refreshImagePreview(): Unit =
    imagePreviewPane.getChildren.clear()
    if imageUrls.isEmpty then
      imagePreviewPane.getChildren.addAll(placeholderLabel, addImageButton)
    else
      imageUrls.foreach { url =>
        val imgView = new ImageView(url)
        imgView.setFitWidth(100)
        imgView.setFitHeight(75)
        imgView.setPreserveRatio(true)
        imagePreviewPane.getChildren.add(imgView)
      }
      imagePreviewPane.getChildren.add(addImageButton)

  /** Reset form for new post */
  def clearForm(): Unit =
    if currentPost != null then
      productNameField.textProperty().unbindBidirectional(currentPost.productName)
      productDescriptionField.textProperty().unbindBidirectional(currentPost.productDescription)
      priceField.textProperty().unbindBidirectional(currentPost.price)
    currentPost = null
    productNameField.clear()
    productDescriptionField.clear()
    priceField.clear()
    clearImages()
    selectedCategories.clear()
    updateCategoryCheckboxes()

  def setProductPost(post: ProductPost): Unit =
    if currentPost != null then
      productNameField.textProperty.unbindBidirectional(currentPost.productName)
      productDescriptionField.textProperty.unbindBidirectional(currentPost.productDescription)
      priceField.textProperty.unbindBidirectional(currentPost.price)


    currentPost = post
    editing = true

    productNameField.textProperty().bindBidirectional(currentPost.productName)
    productDescriptionField.textProperty().bindBidirectional(currentPost.productDescription)
//    Bindings.bindBidirectional(priceField.textProperty(), currentPost.price.delegate, new DoubleStringConverter())

    clearImages()
    imageUrls ++= post.imageURLs
    refreshImagePreview()
    
    // Set selected categories
    selectedCategories.clear()
    selectedCategories ++= post.categories
    updateCategoryCheckboxes()

  def handleOk(): Unit =
    val name = Option(productNameField.getText).map(_.trim).getOrElse("")
    val desc = Option(productDescriptionField.getText).map(_.trim).getOrElse("")
    val priceText = Option(priceField.getText).map(_.trim).getOrElse("")
    val priceParsedOpt = scala.util.Try(priceText.toDouble).toOption

    if name.isEmpty || priceParsedOpt.isEmpty then
      new Alert(Alert.AlertType.Warning):
        initOwner(stage.orNull)
        title = "Invalid input"
        headerText = "Please fill in required fields"
        contentText = "Product name and a valid numeric price are required."
      .showAndWait()
      return

    val now = byehunger.util.DateUtil.nowDateTime

    if editing && currentPost != null then
      // Update existing product
      currentPost.productName.value = name
      currentPost.productDescription.value = desc
      currentPost.price.value = priceParsedOpt.get
      currentPost.imageURLs.clear()
      currentPost.imageURLs ++= imageUrls
      currentPost.categories.clear()
      currentPost.categories ++= selectedCategories
      currentPost.date.value = now
      currentPost.save()
    else
      // Create new product (needs RestaurantPost)
      val r = restaurant.getOrElse {
        new Alert(Alert.AlertType.Error):
          initOwner(stage.orNull)
          title = "Missing context"
          headerText = "No restaurant selected"
          contentText = "A restaurant must be selected to create a product."
        .showAndWait()
        return
      }

      val product = new ProductPost(
        _id = byehunger.model.ProductPost.generateId(),
        _title = name,
        _restaurantPostID = r.restaurantPostID.value
      )
      product.productDescription.value = desc
      product.price.value = priceParsedOpt.get
      product.imageURLs.clear()
      product.imageURLs ++= imageUrls
      product.categories.clear()
      product.categories ++= selectedCategories
      product.date.value = now
      product.save()

      // Immediately reflect in the table
      MainApp.productPost += product

      clearForm()
      stage.foreach(_.close())

  /** Update category checkboxes based on selected categories */
  private def updateCategoryCheckboxes(): Unit =
    if categoriesContainer != null then
      categoriesContainer.getChildren.forEach { node =>
        node match
          case checkbox: CheckBox =>
            val category = checkbox.getUserData.asInstanceOf[Category]
            val isSelected = selectedCategories.contains(category)
            checkbox.setSelected(isSelected)
            // Update visual style based on selection
            if isSelected then
              checkbox.setStyle("-fx-padding: 5 8; -fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-radius: 3;")
            else
              checkbox.setStyle("-fx-padding: 5 8; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 3;")
          case _ => // ignore non-checkbox nodes
      }


  def handleCancel(): Unit =
    stage.foreach(_.close())
