package byehunger.model

import scalafx.beans.property.{IntegerProperty}
import byehunger.model.ProductPost

final case class CartItem(product: ProductPost, quantity: IntegerProperty = IntegerProperty(1)) {
  def unitPrice: Double = Option(product.price.value).getOrElse(0.0)
  def lineTotal: Double = unitPrice * quantity.value
}