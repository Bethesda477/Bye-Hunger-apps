package byehunger.model

import byehunger.util.Database
import byehunger.util.DateUtil.parseLocalDateTime
import scalafx.beans.property.{IntegerProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.image.Image
import scalikejdbc.*
import byehunger.model.Category

import java.time.{LocalDate, LocalDateTime}

import scala.util.{ Try, Success, Failure }

class RestaurantPost (
                       _id: String = RestaurantPost.generateId(),
                       _title: String,
                     )extends Post(_id, _title) :
  var restaurantPostID = StringProperty(_id)
  var restaurantName = StringProperty(_title)
  var restaurantDescription = StringProperty("some description")  //add new later
  var dateOfCreated: ObjectProperty[LocalDateTime] = ObjectProperty[LocalDateTime](LocalDateTime.now())
  var imageURL = StringProperty("some url")
  var imageURLs: ObservableBuffer[String] = ObservableBuffer[String]()
  var addressURL: StringProperty = StringProperty("addressURL")
  var categories: ObservableBuffer[Category] = ObservableBuffer[Category]()

  var products: ObservableBuffer[ProductPost] = ObservableBuffer[ProductPost]()

  // Keep base Post fields synchronized so gallery tiles (which use Post) see current data.

  // 1) name -> Post.title
//  restaurantName.onChange { (_, _, newValue) =>
//    this.title = Option(newValue).getOrElse("")
//  }

  // 2) date -> Post.dateCreated

//  dateOfCreated.onChange { (_, _, newValue) =>
//    this.dateCreated = newValue
//  }

  // 3) images -> Post.images

//  imageURLs.onChange { (_, _) =>
//    this.images.clear()
//    this.images ++= imageURLs
//  }

  // Also initialize base fields once from current properties to avoid stale values.
  this.title = restaurantName.value
//  this.dateCreated = dateOfCreated.value
//  this.images.clear();
//  this.images ++= imageURLs
//  this.addressURL = addressURL


  def image: Image =
    val url = Option(imageURL.value).getOrElse("")
    if url.nonEmpty then new Image(url) else null

  def addProduct(product: ProductPost): Unit = {
    products += product
  }

  def save() : Try[Int] =
    val imageUrlsCsv: String =
      Option(imageURLs).map(_.mkString(",")).getOrElse("")
    val categoriesCsv: String =
      Option(categories).map(_.map(_.toString).mkString(",")).getOrElse("")

    if (!(isExist)) then  //check if the data is created before
      Try(DB autoCommit { implicit session =>
        sql"""
          insert into RESTAURANTPOST (
            RESTAURANTPOSTID, RESTAURANTNAME, RESTAURANTDESCRIPTION, IMAGEURL, IMAGEURLS, DATE, ADDRESSURL, CATEGORIES
          ) values (
            ${restaurantPostID.value}, ${restaurantName.value}, ${restaurantDescription.value},
            ${imageURL.value}, ${imageUrlsCsv}, ${dateOfCreated.value.toString}, ${addressURL.value}, ${categoriesCsv}
          )
        """.update.apply()
      })
    else
      Try(DB autoCommit { implicit session =>
        sql"""
          update RESTAURANTPOST
          set
            RESTAURANTNAME         = ${restaurantName.value},
            RESTAURANTDESCRIPTION  = ${restaurantDescription.value},
            IMAGEURL               = ${imageURL.value},
            IMAGEURLS              = ${imageUrlsCsv},
            DATE                   = ${dateOfCreated.value.toString},
            ADDRESSURL             = ${addressURL.value},
            CATEGORIES             = ${categoriesCsv}
          where RESTAURANTPOSTID = ${restaurantPostID.value}
        """.update.apply()
      })

  def delete() : Try[Int] =
    if (isExist) then
      Try(DB autoCommit { implicit session =>
        sql"""
          delete from RESTAURANTPOST
          where RESTAURANTPOSTID = ${restaurantPostID.value}
        """.update.apply()
      })
    else
      throw new Exception("Person not Exists in Database")

  def isExist : Boolean =
    DB readOnly { implicit session =>
      // here should use ID as primary key, thats mean here check for primary key only
      sql"""
        select RESTAURANTPOSTID as "restaurantPostID"
        from RESTAURANTPOST
        where RESTAURANTPOSTID = ${restaurantPostID.value}
      """.map(_.string("restaurantPostID")).single.apply()
    } match
      case Some(_) => true
      case None => false

  def addCategory(category: Category): Unit =
    if categories.size < 3 && !categories.contains(category) then
      categories += category

  def removeCategory(category: Category): Unit =
    categories -= category

object RestaurantPost extends Database:
  def apply (
              restaurantPostIDS : String,
              restaurantNameS : String,
              restaurantDescriptionS : String,
              dateOfCreatedS : String,
              imageURLS : String,
              imageURLsS : String,
              addressURLS: String,
              categoriesS: String
            ) : RestaurantPost =

    new RestaurantPost(restaurantPostIDS, restaurantNameS) :
      restaurantDescription.value = restaurantDescriptionS
      imageURL.value = imageURLS
      val urls: Seq[String] =
        Option(imageURLsS)
          .map(_.split(",").iterator.map(_.trim).filter(_.nonEmpty).toSeq)
          .getOrElse(Seq.empty)

      imageURLs.clear()
      imageURLs ++= urls
      dateOfCreated.value   = dateOfCreatedS.parseLocalDateTime.getOrElse(null)
      addressURL.value = addressURLS
      
      // Parse categories
      val categoryNames: Seq[String] =
        Option(categoriesS)
          .map(_.split(",").iterator.map(_.trim).filter(_.nonEmpty).toSeq)
          .getOrElse(Seq.empty)
      
      categories.clear()
      categoryNames.foreach { name =>
        Category.values.find(_.displayName == name).foreach(categories += _)
      }

  def initializeTable() =
    DB autoCommit { implicit session =>
      sql"""
        create table RESTAURANTPOST (
          RESTAURANTPOSTID varchar(40) not null primary key,
          RESTAURANTNAME varchar(128),
          RESTAURANTDESCRIPTION varchar(512),
          IMAGEURL varchar(500),
          IMAGEURLS varchar(2000),
          DATE varchar(64),
          ADDRESSURL varchar(2000),
          CATEGORIES varchar(200)
        )
      """.execute.apply()
    } // this runs once at the beginning. To change the schema, drop the old table first.

  def getAllRestaurantPosts : List[RestaurantPost] = {
    val restaurants =
      DB readOnly { implicit session =>
        sql"""
          select
            RESTAURANTPOSTID as "restaurantPostID",
            RESTAURANTNAME as "restaurantName",
            RESTAURANTDESCRIPTION as "restaurantDescription",
            DATE as "date",
            IMAGEURL as "imageURL",
            IMAGEURLS as "imageURLs",
            ADDRESSURL as "addressURL",
            CATEGORIES as "categories"
          from RESTAURANTPOST
        """
          .map { rs =>
            // Order: restaurantPostID, restaurantName, restaurantDescription, date, imageURL, imageURLs
            RestaurantPost(
              rs.string("restaurantPostID"),
              rs.string("restaurantName"),
              rs.string("restaurantDescription"),
              rs.string("date"),
              rs.string("imageURL"),
              rs.string("imageURLs"),
              rs.string("addressURL"),
              rs.string("categories")
            )
          }
          .list
          .apply()
      }
    DB readOnly { implicit session =>
      attachProducts(restaurants)
    }
    restaurants
  }

  //the restaurantPostID in attachProducts function all is using property name, not sql name
  private def attachProducts(restaurants: Seq[RestaurantPost])(using session: DBSession): Unit =
    if restaurants.isEmpty then return

    val byId = restaurants.map(r => r.restaurantPostID.value -> r).toMap
    val ids = restaurants.iterator.map(_.restaurantPostID.value).toSeq
    // Build an IN (...) safely with bound parameters
    val inIds = sqls.in(sqls"p.RESTAURANTPOSTID", ids)

    sql"""
      select
        p.PRODUCTID as "productID",
        p.PRODUCTNAME as "productName",
        p.PRODUCTDESCRIPTION as "productDescription",
        p.PRICE as "price",
        p.IMAGEURLS as "imageURLs",
        p.DATE as "date",
        p.RESTAURANTPOSTID as "restaurantPostID",
        p.CATEGORIES as "categories"
      from PRODUCTPOST p
      where ${inIds}
    """
      .map { rs =>
        val rid = rs.string("restaurantPostID")
        byId.get(rid).foreach { rp =>
          val p = ProductPost(
            productIDS = rs.string("productID"),
            productNameS = rs.string("productName"),
            productDescriptionS = rs.string("productDescription"),
            restaurantPostIDS = rs.string("restaurantPostID"),
            priceS = rs.double("price"),
            dateS = rs.string("date"),
            imageURLsS = rs.string("imageURLs"),
            categoriesS = rs.string("categories")
          )

          Option(rs.string("productDescription")).foreach(desc => p.productDescription.value = desc)

          Option(rs.string("imageURLs"))
            .foreach { s =>
              val urls = s.split(",").iterator.map(_.trim).filter(_.nonEmpty).toSeq
              p.imageURLs.clear()
              p.imageURLs ++= urls
            }

          rp.addProduct(p)
        }
      }
      .list
      .apply()

  def generateId(): String =
    val hex = java.util.UUID.randomUUID().toString.replace("-", "").toUpperCase
    s"POST-${hex.take(6)}"