package byehunger.util
import scalikejdbc.*
import byehunger.model.*
import byehunger.model.RestaurantPost.*

import java.nio.file.{Files, Paths}
import scala.util.Try

trait Database :
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  // Persist DB under the project directory: ./data/myDB

  val dbURL = "jdbc:derby:myDB;create=true;";
  // initialize JDBC driver & connection pool
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "me", "mine")
  given AutoSession = AutoSession

object Database extends Database :
  /** Idempotent setup. Creates table only if it doesn't exist. */
  def setupDB(): Unit =
    if !hasRestaurantDBInitialize then
      try
        RestaurantPost.initializeTable()
        println("[DB] Created table restaurantPost")
      catch
        case ex: Throwable =>
          // If there’s a race or case mismatch, log and continue
          System.err.println(s"[DB] Failed to create table (might already exist): ${ex.getMessage}")

    if !hasProductDBInitialize then
      try
        ProductPost.initializeTable()
        println("[DB] Created table productPost")
      catch
        case ex: Throwable =>
          System.err.println(s"[DB] Failed to create product table (might already exist): ${ex.getMessage}")

    ensureAddressUrlColumn()
    
  /** Check table existence in a Derby-friendly way (unquoted identifiers are uppercased). */
  def hasRestaurantDBInitialize: Boolean =
    DB getTable "RESTAURANTPOST" match
      case Some(_) => true
      case None    => false

  def hasProductDBInitialize: Boolean =
    DB getTable "PRODUCTPOST" match
      case Some(_) => true
      case None => false

  private def enableSqlLogging(): Unit =
    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
      enabled = true,
      singleLineMode = false,
      logLevel = "debug"
    )

  private def ensureAddressUrlColumn(): Unit = {
    val hasColumn = Try {
      DB readOnly { implicit s =>
        // if success, means fetched successfully
        sql"select CATEGORIES from RESTAURANTPOST fetch first row only".map(_.string(1)).single.apply()
        sql"select CATEGORIES from PRODUCTPOST fetch first row only".map(_.string(1)).single.apply()

      }
    }.isSuccess

    if (!hasColumn) {
      DB autoCommit { implicit s =>
        sql"alter table RESTAURANTPOST add column CATEGORIES varchar(200)".execute.apply()
        sql"alter table PRODUCTPOST add column CATEGORIES varchar(200)".execute.apply()

      }
    }
  }

// Notes:
// - derby.system.home pins the on-disk DB to ~/.byehunger/myDB (stable across runs).
// - Call Database.initAndVerify() once at app startup before any DB queries.