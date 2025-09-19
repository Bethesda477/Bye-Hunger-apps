package byehunger.view

import javafx.fxml.FXML
import javafx.scene.control.{Label, RadioButton, ToggleGroup}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{HBox, VBox}
import scalafx.Includes.*
import scalafx.scene.control.Alert.AlertType
import scalafx.stage.Stage
import byehunger.MainApp
import byehunger.model.CartItem
import byehunger.util.SoundEffect
import scalafx.scene.control.Alert

class CartController():
  @FXML private var cartContainer: VBox = _
  @FXML private var totalLabel: Label = _
  var stage: Option[Stage] = None

  def initialize(): Unit =
    render()
    MainApp.cartItems.onChange { (_, _) => render() }

  private def render(): Unit =
    if cartContainer == null || totalLabel == null then return
    cartContainer.getChildren.clear()
    MainApp.cartItems.foreach(ci => cartContainer.getChildren.add(buildRow(ci)))
    totalLabel.setText(f"RM${MainApp.cartTotal}%.2f")

  private def buildRow(ci: CartItem): HBox =
    val firstImageUrl = ci.product.imageURLs.headOption.getOrElse("")
    val iv = new ImageView(
      if firstImageUrl.trim.nonEmpty then new Image(firstImageUrl, 80, 60, true, true)
      else new Image("data:,", 80, 60, true, true)
    )
    iv.setStyle("-fx-background-radius: 8; -fx-background-color: #f8f9fa;")
    
    val name = new Label(Option(ci.product.productName.value).getOrElse(""))
    name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;")
    
    val price = new Label(f"RM${ci.product.price.value}%.2f")
    price.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 14px;")
    
    val qty = new Label(s"x${ci.quantity.value}")
    qty.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-size: 12px;")
    
    val row = new HBox(iv, name, qty, price)
    row.setSpacing(15)
    row.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);")
    row

  @FXML
  def handlePay(): Unit = {
    // 播放支付成功音效
    SoundEffect.playPaymentSuccess()
    
//    val selected = Option(paymentToggle.getSelectedToggle).collect { case rb: RadioButton => rb.getText }.getOrElse("Card")
    new Alert(AlertType.Information) {
      initOwner(MainApp.stage)
      title = "Payment"
      headerText = "Payment Successful"
      contentText = f"Total: RM${MainApp.cartTotal}%.2f"
    }.showAndWait()
    MainApp.cartItems.clear()
    stage.foreach(_.close())
  }
