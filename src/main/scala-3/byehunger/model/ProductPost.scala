package byehunger.model

import byehunger.util.DateUtil.parseLocalDateTime
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.image.Image
import scalikejdbc.*
import byehunger.model.Category

import java.time.LocalDateTime
import scala.util.Try


class ProductPost(
                   _id: String = ProductPost.generateId(),
                   _title: String,
                   _restaurantPostID: String = RestaurantPost.generateId()
                 ) extends Post(_id, _title):
  var productID: StringProperty = StringProperty(_id)
  var productName: StringProperty = StringProperty(_title)
  var productDescription: StringProperty = StringProperty("some description")
  val restaurantPostID: StringProperty = StringProperty(_restaurantPostID)
  var price: ObjectProperty[Double] = ObjectProperty[Double](0.0)
  var date: ObjectProperty[LocalDateTime] = ObjectProperty[LocalDateTime](LocalDateTime.now())
  var imageURLs: ObservableBuffer[String] = ObservableBuffer[String]()
  var categories: ObservableBuffer[Category] = ObservableBuffer[Category]()


  def save(): Try[Int] =
    val imageUrlsCsv: String =
      Option(imageURLs).map(_.mkString(",")).getOrElse("")
    val categoriesCsv: String =
      Option(categories).map(_.map(_.toString).mkString(",")).getOrElse("")
    val dateStr = Option(date.value).map(byehunger.util.DateUtil.DATE_TIME_FORMATTER.format).getOrElse("")


    if (!(isExist)) then { //check if the data is created before
      Try(DB autoCommit { implicit session =>
        sql"""
            insert into PRODUCTPOST (
              PRODUCTID, PRODUCTNAME, PRODUCTDESCRIPTION, RESTAURANTPOSTID, PRICE, DATE, IMAGEURLS, CATEGORIES
            ) values (
              ${productID.value}, ${productName.value}, ${productDescription.value},
              ${restaurantPostID.value}, ${price.value}, ${dateStr}, ${imageUrlsCsv}, ${categoriesCsv}
            )
          """.update.apply()
      })
      ///here insert..... productID... must same as initializeTable()...line 106
    } else
      Try(DB autoCommit { implicit session =>
        sql"""
            update PRODUCTPOST
            set
              PRODUCTNAME         = ${productName.value},
              PRODUCTDESCRIPTION  = ${productDescription.value},
              PRICE               = ${price.value},
              DATE                   = ${dateStr},
              IMAGEURLS              = ${imageUrlsCsv},
              CATEGORIES             = ${categoriesCsv}
            where PRODUCTID = ${productID.value}
          """.update.apply()
      })

  def delete(): Try[Int] =
    if (isExist) then
      Try(DB autoCommit { implicit session =>
        sql"""
            delete from PRODUCTPOST
            where PRODUCTID = ${productID.value}
          """.update.apply()
      })
    else
      throw new Exception("Person not Exists in Database")

  def isExist: Boolean =
    DB readOnly { implicit session =>
      // here should use ID as primary key, thats mean here check for primary key only
      sql"""
          select PRODUCTID from PRODUCTPOST
          where PRODUCTID = ${productID.value}
        """.map(_.string("productID")).single.apply()
    } match
      case Some(_) => true
      case None => false

  def addCategory(category: Category): Unit =
    if categories.size < 3 && !categories.contains(category) then
      categories += category

  def removeCategory(category: Category): Unit =
    categories -= category
// ... existing code ...
object ProductPost:
  def apply(
             productIDS: String,
             productNameS: String,
             restaurantPostIDS: String,
             productDescriptionS: String,
             priceS: Double,
             dateS: String,
             imageURLsS: String,
             categoriesS: String
           ): ProductPost =

    new ProductPost(productIDS, productNameS, restaurantPostIDS) :
      productDescription.value = productDescriptionS
      price.value = priceS
      date.value = dateS.parseLocalDateTime.getOrElse(null)
      val urls: Seq[String] =
        Option(imageURLsS)
          .map(_.split(",").iterator.map(_.trim).filter(_.nonEmpty).toSeq)
          .getOrElse(Seq.empty)
      imageURLs.clear()
      imageURLs ++= urls
      
      // Parse categories
      val categoryNames: Seq[String] =
        Option(categoriesS)
          .map(_.split(",").iterator.map(_.trim).filter(_.nonEmpty).toSeq)
          .getOrElse(Seq.empty)
      
      categories.clear()
      categoryNames.foreach { name =>
        Category.values.find(_.displayName == name).foreach(categories += _)
      }


  def initializeTable(): Unit =
    DB autoCommit { implicit session =>
      sql"""
        create table PRODUCTPOST (
          PRODUCTID varchar(40) not null primary key,
          PRODUCTNAME varchar(128),
          PRODUCTDESCRIPTION varchar(1024),
          RESTAURANTPOSTID varchar(40) not null,
          PRICE decimal(10,2),
          DATE varchar(64),
          IMAGEURLS varchar(2000),
          CATEGORIES varchar(200),
          constraint FK_PRODUCT_RESTAURANT
            foreign key (RESTAURANTPOSTID)
            references RESTAURANTPOST(RESTAURANTPOSTID)
        )
      """.execute.apply()
    }

  def getAllProductPosts: List[ProductPost] = {
    val products =
      DB readOnly { implicit session =>
        sql"select * from PRODUCTPOST"
          .map { rs =>
            // Order: postID, restaurantName, restaurantDescription, date, imageURL, imageURLs
            ProductPost(
              rs.string("productID"),
              rs.string("productName"),
              rs.string("restaurantPostID"),
              rs.string("productDescription"),
              rs.double("price"),
              rs.string("date"),
              rs.string("imageURLs"),
              rs.string("categories")
            )
          }
          .list
          .apply()
      }

    products
  }
  // Generates IDs like "PROD-3F9A1C8E2B47"
  def generateId(): String =
    val hex = java.util.UUID.randomUUID().toString.replace("-", "").toUpperCase
    s"PROD-${hex.take(12)}"
