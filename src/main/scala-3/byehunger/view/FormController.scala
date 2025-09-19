package byehunger.view

import javafx.fxml.FXML
import byehunger.MainApp
import byehunger.model.RestaurantPost
import javafx.event.ActionEvent
import javafx.scene.control.{CheckBox, Label, TextField}
import javafx.scene.image.{Image, ImageView}
import javafx.stage.{FileChooser, Stage}
import scalafx.scene.control.Alert
import scalafx.collections.ObservableBuffer
import scalafx.Includes.*
import byehunger.model.Category
import javafx.scene.layout.{FlowPane, HBox}

import java.io.File

class FormController() extends ImageManagement:

  @FXML private var restaurantDescriptionField: TextField = null
  @FXML private var restaurantNameField: TextField = null
  @FXML private var imagePreviewPane: javafx.scene.layout.HBox = null
  @FXML private var addressURL: TextField = _
  @FXML private var categoriesContainer: FlowPane = _

  // Multiple images
  var imageUrls: ObservableBuffer[String] = ObservableBuffer.empty[String]
  var selectedCategories: ObservableBuffer[Category] = ObservableBuffer.empty[Category]

  var stage: Option[Stage] = None
  
  // Implement abstract methods required by ImageManagement trait
  override def getImageUrls: ObservableBuffer[String] = this.imageUrls
  override def getStage: Option[Stage] = this.stage
  override def getImagePreviewPane: HBox = this.imagePreviewPane
  private var editing: Boolean = false
  private var currentPost: RestaurantPost = null

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
      val label = new Label("Choose Categories (max 3):")
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
                headerText = "You can only choose at most 3 categories"
                contentText = "Please unselect other first before continue to select"
              .showAndWait()
          else
            selectedCategories -= category
            checkbox.setStyle("-fx-padding: 5 8; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 3;")
        })
        
        categoriesContainer.getChildren.add(checkbox)
      }



  /** Pre-fill form when editing */
  def setPost(post: RestaurantPost): Unit =
    if currentPost != null then
      // No need to unbind since we're not using binding anymore
      // Just clear the fields
      restaurantDescriptionField.clear()
      restaurantNameField.clear()
      addressURL.clear()

    currentPost = post
    editing = true

    // Set text field values directly (like images and categories)
    restaurantNameField.setText(post.restaurantName.value)
    restaurantDescriptionField.setText(post.restaurantDescription.value)
    addressURL.setText(post.addressURL.value)

    // Debug: Print current values to verify
    println(s"Setting form for: ${post.restaurantName.value}")
    println(s"Description: ${post.restaurantDescription.value}")
    println(s"Address: ${post.addressURL.value}")

    clearImages()
    imageUrls ++= post.imageURLs
    refreshImagePreview()
    
    // Set selected categories
    selectedCategories.clear()
    selectedCategories ++= post.categories
    updateCategoryCheckboxes()
    
    // Force UI refresh
    refreshUI()

  /** Reset form for new post */
  def clearForm(): Unit =
    // No need to unbind since we're not using binding anymore
    editing = false
    currentPost = null
    restaurantDescriptionField.clear()
    restaurantNameField.clear()
    addressURL.clear()
    clearImages()
    selectedCategories.clear()
    updateCategoryCheckboxes()

  /** Save changes */
  @FXML
  def handleOk(action: ActionEvent): Unit =
    if isInputValid() then
      if editing && currentPost != null then
        // Ensure the bound values are properly updated in the model
        // Get current values from text fields to ensure they're saved
        val currentName = restaurantNameField.getText
        val currentDescription = restaurantDescriptionField.getText
        val currentAddress = addressURL.getText
        
        // Update the model with current values
        currentPost.restaurantName.value = currentName
        currentPost.restaurantDescription.value = currentDescription
        currentPost.addressURL.value = currentAddress
        
        // Update collections
        currentPost.imageURLs.clear()
        currentPost.imageURLs ++= imageUrls
        currentPost.categories.clear()
        currentPost.categories ++= selectedCategories
        
        // Save to database
        currentPost.save()
        
        println(s"Saved restaurant: ${currentName}, Description: ${currentDescription}, Address: ${currentAddress}")
      else {
        val newPost = new RestaurantPost(_title = restaurantNameField.getText)
        newPost.restaurantDescription.value = restaurantDescriptionField.getText
        newPost.addressURL.value = addressURL.getText
        newPost.imageURLs.clear()
        newPost.imageURLs ++= imageUrls
        newPost.categories.clear()
        newPost.categories ++= selectedCategories

        newPost.save()
        MainApp.restaurantPost += newPost

      }

      clearForm()
      stage.foreach(_.close())

  /** Cancel */
  @FXML
  def handleCancel(action: ActionEvent): Unit =
    clearForm()
    stage.foreach(_.close())

  /** Force UI refresh after binding changes */
  private def refreshUI(): Unit =
    // Force JavaFX to refresh the UI
    if restaurantDescriptionField != null then
      restaurantDescriptionField.requestFocus()
      restaurantDescriptionField.deselect()
    if restaurantNameField != null then
      restaurantNameField.requestFocus()
      restaurantNameField.deselect()
    if addressURL != null then
      addressURL.requestFocus()
      addressURL.deselect()

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

  /** Validation */
  private def isInputValid(): Boolean =
    var err = ""
    if restaurantDescriptionField.getText == null || restaurantDescriptionField.getText.trim.isEmpty then
      err += "No valid description!\n"
    if restaurantNameField.getText == null || restaurantNameField.getText.trim.isEmpty then
      err += "No valid restaurant name!\n"
    if addressURL.getText == null || addressURL.getText.trim.isEmpty then
      err += "No valid address!\n"

    if err.isEmpty then true
    else
      new Alert(Alert.AlertType.Error):
        initOwner(stage.orNull)
        title = "Invalid Fields"
        headerText = "Please correct invalid fields"
        contentText = err
      .showAndWait()
      false