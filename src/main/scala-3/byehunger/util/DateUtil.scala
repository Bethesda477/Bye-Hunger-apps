package byehunger.util

import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.time.format.{DateTimeFormatter, DateTimeParseException}

object DateUtil :
  // Date-only
  val DATE_PATTERN       = "dd.MM.yyyy"
  val DATE_FORMATTER     = DateTimeFormatter.ofPattern(DATE_PATTERN)

  // Date + Time
  val DATE_TIME_PATTERN  = "dd.MM.yyyy HH:mm:ss"
  val DATE_TIME_FORMATTER= DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)

  // Time-only
  val TIME_PATTERN       = "HH:mm:ss"
  val TIME_FORMATTER     = DateTimeFormatter.ofPattern(TIME_PATTERN)

  // Current values
  def today: LocalDate = LocalDate.now()
  def nowDateTime: LocalDateTime = LocalDateTime.now()
  def nowTime: LocalTime = LocalTime.now()

  // Convenience formatted strings for "now"
  def todayString: String = DATE_FORMATTER.format(today)
  def nowDateTimeString: String = DATE_TIME_FORMATTER.format(nowDateTime)
  def nowTimeString: String = TIME_FORMATTER.format(nowTime)

  // Extensions for formatting/parsing

  extension (date: LocalDate)
    /**
     * Returns the given date as a well formatted String using DATE_PATTERN.
     */
    def asString: String =
      if date == null then null
      else DATE_FORMATTER.format(date)

  extension (dt: LocalDateTime)
    /**
     * Returns the given date-time as a well formatted String using DATE_TIME_PATTERN.
     */
    def asDateTimeString: String =
      if dt == null then null
      else DATE_TIME_FORMATTER.format(dt)

  extension (t: LocalTime)
    /**
     * Returns the given time as a well formatted String using TIME_PATTERN.
     */
    def asTimeString: String =
      if t == null then null
      else TIME_FORMATTER.format(t)

  extension (data: String)
    /**
     * Converts a String in DATE_PATTERN to an Option[LocalDate].
     */
    def parseLocalDate: Option[LocalDate] =
      try Option(LocalDate.parse(data, DATE_FORMATTER))
      catch case _: DateTimeParseException => None

    /**
     * Converts a String in DATE_TIME_PATTERN to an Option[LocalDateTime].
     */
    def parseLocalDateTime: Option[LocalDateTime] =
      try Option(LocalDateTime.parse(data, DATE_TIME_FORMATTER))
      catch case _: DateTimeParseException => None

    /**
     * Converts a String in TIME_PATTERN to an Option[LocalTime].
     */
    def parseLocalTime: Option[LocalTime] =
      try Option(LocalTime.parse(data, TIME_FORMATTER))
      catch case _: DateTimeParseException => None

    def isValidDate: Boolean = data.parseLocalDate.isDefined
    def isValidDateTime: Boolean = data.parseLocalDateTime.isDefined
    def isValidTime: Boolean = data.parseLocalTime.isDefined
//Static method????
//subtype polymophysim , dont use list, use iterable

//option is abstract class, under it has Some and None, concrete class

//extension = allows add new methods only to an existing class
//why extension: extend some existing old method*************