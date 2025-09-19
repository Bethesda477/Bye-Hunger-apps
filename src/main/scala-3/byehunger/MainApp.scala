package byehunger

import byehunger.model.{CartItem, ProductPost, RestaurantPost}
import byehunger.util.Database
import byehunger.util.MapNavigator
import byehunger.util.SoundEffect
import byehunger.view.{AboutController, ActivityController, FormController, LoginController, MainWindowController, MerchantLoginController, MerchantMainWindowController, MerchantSignUpController, MessageIconController, ProductFormController, ProductOverviewController, ProductPostController, RestaurantPostController, SignUpController, FirstPageController, RestaurantController, FoodController}
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.{Node, Scene}
import scalafx.Includes.*
import scalafx.scene.image.Image
import javafx.scene as jfxs
import javafx.scene.Parent
import javafx.scene.layout.{AnchorPane, StackPane}
import javafx.stage.{Modality, StageStyle}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.Alert
import scalafx.scene.layout.TilePane
import scalafx.stage.Modality.ApplicationModal
import scalafx.stage.{Popup, Stage, Window}

import java.net.{URL, URLEncoder}
import java.nio.charset.StandardCharsets

object MainApp extends JFXApp3:
//  Database.initAndVerify()
  Database.setupDB()

  var rootPane: Option[javafx.scene.layout.BorderPane] = None

  var cssResource = getClass.getResource("/byehunger/view/style.css")

  var loginController: Option[LoginController] = None
  var mainWindowController: Option[MainWindowController] = None
  var activityController: Option[ActivityController] = None
  var formController: Option[FormController] = None
  var restaurantPostController: Option[RestaurantPostController] = None
  var merchantLoginController: Option[MerchantLoginController] = None
  var productOverviewController: Option[ProductOverviewController] = None
  var merchantMainWindowController: Option[MerchantMainWindowController] = None
  var userChoiceController: Option[FirstPageController] = None
  var restaurantController: Option[RestaurantController] = None
  var foodController: Option[FoodController] = None




  val restaurantPost = ObservableBuffer[RestaurantPost]()
  val productPost = ObservableBuffer[ProductPost]()
//  val restaurantPostModel = RestaurantPost() //no new meh?
  val favouritePost = ObservableBuffer[RestaurantPost]()
  val favouriteProductPost = ObservableBuffer[ProductPost]()
  val cartItems: ObservableBuffer[CartItem] = ObservableBuffer.empty

  val favouriteClicksByRestaurant: scala.collection.mutable.Map[String, Int] = scala.collection.mutable.Map.empty
  val navigationClicksByRestaurant: scala.collection.mutable.Map[String, Int] = scala.collection.mutable.Map.empty



  override def start(): Unit =
    SoundEffect.initialize()
//    SoundEffect.playAlertAudio() // or any other sound method
//
//    SoundEffect.testSoundEffects()
    

    val rootLayoutResource: URL = getClass.getResource("view/RootLayout.fxml")
    val loader = new FXMLLoader(rootLayoutResource)
    loader.load()
    rootPane = Option(loader.getRoot[jfxs.layout.BorderPane]) //initilize
    stage = new PrimaryStage():
      title = "Bye Hunger"
      icons += new Image(getClass.getResource("/images/logo2.png").toExternalForm)
      scene = new Scene():
        root = rootPane.get
        stylesheets = Seq(cssResource.toExternalForm)

    showFirstPage()


  def showLogin(): Unit =
    val resource = getClass.getResource("/byehunger/view/Login.fxml")
    val loader = new FXMLLoader(resource)
//    loader.load()
    val pane = loader.load[jfxs.layout.AnchorPane]()
    loginController = Option(loader.getController[LoginController])
//    this.rootPane.get.center = roots //auto conversion from Javafx to scalafx
    //first rootPane is BorderPane.center = AnchorPane
    //or
    rootPane.foreach(_.setCenter(pane))  //better??

  def showSignUp(): Boolean =
    val resource = getClass.getResource("/byehunger/view/SignUp.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[jfxs.layout.Pane]()
    
    val signupWindow = new Stage():
      initOwner(stage)
      initModality(ApplicationModal)
      title = "Sign Up"
      scene = new Scene:
        root = pane
        stylesheets = Seq(cssResource.toExternalForm)


    val signUpController = loader.getController[SignUpController]()
    signUpController.stage = Option(signupWindow)
    signupWindow.showAndWait()
    signUpController.onClicked

  def showMerchantSignUp(): Boolean =
    val resource = getClass.getResource("/byehunger/view/MerchantSignUp.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[jfxs.layout.Pane]()

    val mywindow = new Stage():
      initOwner(stage)
      initModality(ApplicationModal)
      title = "Merchant Sign Up"
      scene = new Scene:
        root = pane
        stylesheets = Seq(cssResource.toExternalForm)


    val ctrl = loader.getController[MerchantSignUpController]()
    ctrl.stage = Option(mywindow)
    mywindow.showAndWait()
    ctrl.onClicked
    
  def showFirstPage(): Unit =
    val resource = getClass.getResource("/byehunger/view/FirstPage.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[javafx.scene.layout.AnchorPane]()
    userChoiceController = Option(loader.getController[FirstPageController])
    rootPane.foreach(_.setCenter(pane))

  def backToFirstPage(): Unit =
    showFirstPage()

  def showMerchantLogin(): Unit =
    val resource = getClass.getResource("/byehunger/view/MerchantLogin.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[jfxs.layout.Pane]()
    merchantLoginController = Option(loader.getController[MerchantLoginController])
    rootPane.foreach(_.setCenter(pane))


  def showMainWindow(): Unit =
    val resource = getClass.getResource("/byehunger/view/MainWindow.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[jfxs.layout.BorderPane]()
    mainWindowController = Option(loader.getController[MainWindowController])
    rootPane.foreach(_.setCenter(pane))
    SoundEffect.playLoginAudio()


  def showMerchantMainWindow(): Unit =
    val resource = getClass.getResource("/byehunger/view/MerchantMainWindow.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[jfxs.layout.BorderPane]()
    merchantMainWindowController = Option(loader.getController[MerchantMainWindowController])
    rootPane.foreach(_.setCenter(pane))


  def showAbout(): Boolean =
    val about = getClass.getResource("/byehunger/view/About.fxml")
    val loader = new FXMLLoader(about)
    loader.load()

    val pane = loader.getRoot[jfxs.layout.AnchorPane]()
    val mywindow = new Stage():  //why scalafx here
      initOwner(stage)
      initModality(ApplicationModal)
      title = "About"
      scene = new Scene():
        root = pane
        stylesheets = Seq(cssResource.toExternalForm)

    val ctrl = loader.getController[AboutController]()
    ctrl.stage = Option(mywindow)
    mywindow.showAndWait()
    ctrl.onClicked


  def showForm(existing: Option[RestaurantPost] = None): Unit =
    val form = getClass.getResource("/byehunger/view/Form.fxml")
    val loader = new FXMLLoader(form)
    loader.load()
//    val pane = loader.load[jfxs.layout.AnchorPane]()   //ask which one better

    val pane = loader.getRoot[jfxs.layout.AnchorPane]()
    val mywindow = new Stage(): //why scalafx here
      initOwner(stage)
      initModality(ApplicationModal)
      title = if existing.isDefined then "Edit Post" else "Add Post"
      scene = new Scene():
        root = pane
        stylesheets = Seq(cssResource.toExternalForm)

    val ctrl = loader.getController[FormController]()
    ctrl.stage = Option(mywindow)
    existing match {
      case Some(post) => ctrl.setPost(post)
      case None => ctrl.clearForm()

    }
    mywindow.showAndWait()

  def showProductForm(selectedRestaurant: RestaurantPost, existingProduct: Option[ProductPost] = None): Option[ProductPost] =
    val resource = getClass.getResource("/byehunger/view/ProductForm.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()

    val pane = loader.getRoot[javafx.scene.layout.Pane]()
    val ctrl = loader.getController[ProductFormController]()
    ctrl.restaurant = Some(selectedRestaurant)

//    existingProduct.foreach(ctrl.setProductPost)


    val dialog = new Stage():
      initOwner(stage)
      initModality(ApplicationModal)
      title = if existingProduct.isDefined then "Edit Product" else "Add Product"
      scene = new Scene():
        root = pane
        stylesheets = Seq(cssResource.toExternalForm)

    ctrl.stage = Option(dialog)
    existingProduct match {
      case Some(post) => ctrl.setProductPost(post)
      case None => ctrl.clearForm()

    }
    dialog.showAndWait()

    // Return last saved product (if any)
    existingProduct // modify to return created product if needed

  def showRestaurant(): Unit =
    val resource = getClass.getResource("/byehunger/view/Restaurant.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[jfxs.layout.BorderPane]()
    restaurantController = Option(loader.getController[RestaurantController])
    rootPane.foreach(_.setCenter(pane))

  def showFood(): Unit =
    val resource = getClass.getResource("/byehunger/view/Food.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[jfxs.layout.BorderPane]()
    foodController = Option(loader.getController[FoodController])
    rootPane.foreach(_.setCenter(pane))

//  def showRestaurantPost(): Unit =
//    val form = getClass.getResource("/byehunger/view/RestaurantPost.fxml")
//    val loader = new FXMLLoader(form)
//    loader.load()
//
//    val pane = loader.getRoot[jfxs.layout.AnchorPane]()
//    val mywindow = new Stage(): //why scalafx here
//      initOwner(stage)
//      initModality(ApplicationModal)
//      title = "Restaurant Post"
//      scene = new Scene():
//        root = pane

//    val ctrl = loader.getController[RestaurantPostController]()
//    ctrl.stage = Option(mywindow)
//    mywindow.showAndWait()
//    ctrl.onClicked



  def showCommunityActivity(): Boolean = ???

  def showMessage(): Boolean = ???

  def showOfferRequest(): Boolean = ???

  def showPostDetail(): Boolean = ???


  def showPopup(fxmlPath: String, ownerNode: Node): Unit =
    val resource: URL = getClass.getResource(fxmlPath)
    val loader = new FXMLLoader(resource)
    loader.load()

    //2. get the root pane(most outside pane) of the targeted window from FXML file
    val pane = loader.getRoot[jfxs.layout.AnchorPane]()
    pane.stylesheets.add(cssResource.toExternalForm)


    //    val formController = loader.getController[FormController]
//    formController.form = RestaurantPost()


    //3. Create Popup window
    val popup = new Popup():
      content.add(pane)
      autoHide = true


    //4. Show popup window relative to the button was clicked
    val ownerWindow: Window = ownerNode.getScene.getWindow
    val nodeBounds = ownerNode.localToScreen(ownerNode.boundsInLocal.value)
    popup.show(ownerWindow, nodeBounds.getMinX, nodeBounds.getMaxY)


  def showRestaurantPostPopup(post: RestaurantPost): Unit =
    SoundEffect.playOpenPostAudio()
    val resource = getClass.getResource("/byehunger/view/RestaurantPost.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[javafx.scene.layout.AnchorPane]()
    val ctrl = loader.getController[RestaurantPostController]()

    val popupStage = new Stage():
      initOwner(stage) // main window stage
      initModality(ApplicationModal)
      title = "Restaurant Post"
      scene = new Scene():
        root = pane
        stylesheets = Seq(cssResource.toExternalForm)

    ctrl.stage = Option(popupStage)
    ctrl.post = post

    popupStage.showAndWait()

  def showProductOverview(post: ProductPost): Unit =
    SoundEffect.playOpenPostAudio()
    val resource = getClass.getResource("/byehunger/view/ProductOverview.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[javafx.scene.layout.Pane]()
    val ctrl = loader.getController[ProductOverviewController]()

    // Show in non-embedded mode (standalone window)
    ctrl.show(post, embedded = false)

    val popupStage = new Stage():
      initOwner(stage)
      initModality(ApplicationModal)
      title = "Product Post"
      scene = new Scene():
        root = pane
        stylesheets = Seq(cssResource.toExternalForm)


    ctrl.stage = Option(popupStage)
    popupStage.showAndWait()


  def replaceWithRestaurantForProduct(product: ProductPost, targetStage: scalafx.stage.Stage): Unit =
    val rid = Option(product.restaurantPostID.value).getOrElse("")
    val parentOpt = restaurantPost.find(r => Option(r.restaurantPostID.value).contains(rid))

    parentOpt.foreach { parent =>
      val resource = getClass.getResource("/byehunger/view/RestaurantPost.fxml")
      val loader = new FXMLLoader(resource)
      val pane = loader.load[javafx.scene.layout.AnchorPane]()
      val ctrl = loader.getController[RestaurantPostController]()

      // Wire controller to the stage and data first
      ctrl.stage = Option(targetStage)
      ctrl.post = parent

      // Reuse existing window: replace root instead of opening a modal
      if targetStage.scene.value == null then
        targetStage.scene = new scalafx.scene.Scene {
          root = pane
        }
      else
        targetStage.scene.value.setRoot(pane)

      targetStage.title = "Restaurant Post"
      targetStage.show()
    }
  def showRestaurantForProduct(product: ProductPost): Unit =
    // 1) Resolve parent restaurant by restaurantPostID
    val rid = Option(product.restaurantPostID.value).getOrElse("")
    val parentOpt = restaurantPost.find(r => Option(r.restaurantPostID.value).contains(rid))

    parentOpt match
      case Some(parent) =>
        // 2) Load the RestaurantPost view
        val resource = getClass.getResource("/byehunger/view/RestaurantPost.fxml")
        val loader = new FXMLLoader(resource)
        val pane = loader.load[javafx.scene.layout.AnchorPane]()
        val ctrl = loader.getController[RestaurantPostController]()

        // 3) Inject the model BEFORE showing the stage so bindings and lists render immediately
        ctrl.post = parent

        // 4) Show as a modal window (same look-and-feel every time)
        val win = new Stage():
          initOwner(stage)
          initModality(ApplicationModal)
          title = "Restaurant Post"
          scene = new Scene():
            root = pane
            stylesheets = Seq(cssResource.toExternalForm)


        ctrl.stage = Option(win)
        win.showAndWait()

      case None =>
        new Alert(Alert.AlertType.Warning):
          initOwner(MainApp.stage)
          title = "Not found"
          headerText = "Restaurant not found"
          contentText = s"No restaurant found for product ${Option(product.productName.value).getOrElse("")}."
        .showAndWait()
    
  def addToFavourites(post: RestaurantPost): Unit = {
    val exists = favouritePost.exists(_.restaurantPostID.value == post.restaurantPostID.value)
    if (!exists) {
      SoundEffect.playFavouriteAudio()
      favouritePost += post
      new Alert(Alert.AlertType.Information) {
        initOwner(MainApp.stage)
        title = "Favourite"
        headerText = "Add successfully to Favourite"
        contentText = Option(post.restaurantName.value).getOrElse("")
      }.showAndWait()
    } else {
      SoundEffect.playAlertAudio()
      new Alert(Alert.AlertType.Information) {
        initOwner(MainApp.stage)
        title = "Favourite"
        headerText = "Already in Favourite"
        contentText = Option(post.restaurantName.value).getOrElse("")
      }.showAndWait()
    }
    val key = Option(post.restaurantPostID.value).getOrElse("")
    favouriteClicksByRestaurant.updateWith(key)(prev => Some(prev.getOrElse(0) + 1))
  }

  def addProductToFavourites(post: ProductPost): Unit = {
    val exists = favouriteProductPost.exists(_.productID.value == post.productID.value)
    if (!exists) {
      SoundEffect.playFavouriteAudio()
      favouriteProductPost += post
      new Alert(Alert.AlertType.Information) {
        initOwner(MainApp.stage)
        title = "Favourite"
        headerText = "Add successfully to Favourite"
        contentText = Option(post.productName.value).getOrElse("")
      }.showAndWait()
    } else {
      SoundEffect.playAlertAudio()
      new Alert(Alert.AlertType.Information) {
        initOwner(MainApp.stage)
        title = "Favourite"
        headerText = "Already in Favourite"
        contentText = Option(post.productName.value).getOrElse("")
      }.showAndWait()
    }
    
  }

  /** Open a map for this post (uses restaurant name as query). */
  def openMapFor(post: RestaurantPost): Unit =
    val url = s"https://www.google.com/maps/search/?api=1&query=${URLEncoder.encode(post.restaurantName.value, StandardCharsets.UTF_8)}"
    val ok = MapNavigator.openUrl(url)
    if (ok) {
      val key = Option(post.restaurantPostID.value).getOrElse("")
      MainApp.navigationClicksByRestaurant.updateWith(key)(prev => Some(prev.getOrElse(0) + 1))
    } else {
      new Alert(Alert.AlertType.Error) {
        SoundEffect.playAlertAudio()
        initOwner(MainApp.stage)
        title = "Open failed"
        headerText = "Could not open the maps application"
        contentText = "Please open your browser and search manually."
      }.showAndWait()
    }

  def addToCart(product: ProductPost): Unit = {
    // 播放添加到购物车的音效
    SoundEffect.playCartAdd()
    // Merge by productID; increase quantity if already in cart
    val pid = Option(product.productID.value).getOrElse("")
    cartItems.find(ci => Option(ci.product.productID.value).contains(pid)) match {
      case Some(ci) => ci.quantity.value = ci.quantity.value + 1
      case None => cartItems += CartItem(product)
    }
    new Alert(Alert.AlertType.Information) {
      initOwner(MainApp.stage)
      title = "Cart"
      headerText = "Added to cart"
      contentText = Option(product.productName.value).getOrElse("")
    }.showAndWait()
  }

  def cartTotal: Double = cartItems.view.map(_.lineTotal).sum

  def showCartWindow(): Unit = {
    val resource = getClass.getResource("/byehunger/view/Cart.fxml")
    val loader = new FXMLLoader(resource)
    val pane = loader.load[javafx.scene.layout.Pane]()
    val ctrl = loader.getController[byehunger.view.CartController]()
    val win = new Stage():
      initOwner(stage)
      title = "Cart"
      scene = new Scene():
        root = pane
        stylesheets = Seq(cssResource.toExternalForm)

    ctrl.stage = Option(win)
    win.show()
  }











