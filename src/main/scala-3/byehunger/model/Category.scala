package byehunger.model

enum Category(val displayName: String):
  case BREAD extends Category("Bread")
  case FRESH_MEAT extends Category("Fresh Meat")
  case FRUIT extends Category("Fruit")
  case LUNCHBOX extends Category("LunchBox")
  case DIMSUM extends Category("Dimsum")
  case KIMCHI extends Category("Marinated Food")
  case MILK_TEA extends Category("Juice")
  case RICE extends Category("Rice")
  case ROAST_DUCK extends Category("Cooked Meat")
  case FRIED_CHICKEN extends Category("Fries")
  case VEGETABLES extends Category("Vegetable")
  case BEVERAGES extends Category("Beverages")
  
  override def toString: String = displayName
