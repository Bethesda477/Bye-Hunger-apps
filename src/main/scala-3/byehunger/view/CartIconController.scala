package byehunger.view

import javafx.fxml.FXML
import javafx.scene.control.{Label, Labeled}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{HBox, VBox}
import byehunger.MainApp
import byehunger.model.CartItem
import scalafx.Includes.*

class CartIconController():
  @FXML private var cartList: VBox = _
  @FXML private var emptyCartLabel: Label = _

  def initialize(): Unit =
    render()
    MainApp.cartItems.onChange { (_, _) => render() }

  private def render(): Unit =
    if cartList == null || emptyCartLabel == null then return
    cartList.getChildren.clear()
    
    if MainApp.cartItems.isEmpty then
      emptyCartLabel.setVisible(true)
    else
      emptyCartLabel.setVisible(false)
      MainApp.cartItems.foreach(ci => cartList.getChildren.add(buildRow(ci)))

  private def buildRow(ci: CartItem): HBox =
    val firstImageUrl = ci.product.imageURLs.headOption.getOrElse("")
    val iv = new ImageView(
      if firstImageUrl.trim.nonEmpty then new Image(firstImageUrl, 50, 40, true, true)
      else new Image("data:,", 50, 40, true, true)
    )
    iv.setStyle("-fx-background-radius: 6; -fx-background-color: #f8f9fa;")
    
    val name = new Label(Option(ci.product.productName.value).getOrElse(""))
    name.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;")
    name.setMaxWidth(150)
    name.setWrapText(true)
    
    val price = new Label(f"RM${ci.product.price.value}%.2f")
    price.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold; -fx-font-size: 11px;")
    
    val qty = new Label(s"x${ci.quantity.value}")
    qty.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-padding: 2 6; -fx-background-radius: 8; -fx-font-size: 10px;")
    
    val row = new HBox(iv, name, qty, price)
    row.setSpacing(8)
    row.setStyle("-fx-padding: 8; -fx-background-color: white; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 1, 0, 0, 1);")
    row.setOnMouseClicked(_ => MainApp.showCartWindow())
    row

  @FXML
  def handleOpenCart(): Unit =
    MainApp.showCartWindow()