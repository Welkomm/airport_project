package models

case class Country(
  id: Int,
  code: String,
  name: String,
  continent: String,
  wikipediaLink: String,
  keywords: Option[String]
)

case class Airport(
  id: Int,
  ident: String,
  name: String,
  countryCode: String,
  latitude: Double,
  longitude: Double,
  elevation: Option[Int]
)

case class Runway(
  id: Int,
  airportRef: Int,
  surface: String,
  leIdent: String,
  length: Option[Int],
  width: Option[Int],
  lighted: Option[Int], 
  closed: Option[Int]
)

object Country {
  def from(line: Array[String]): Option[Country] = 
    if (line.length >= 5) Some(
      Country(
        id = line(0).filter(_.isDigit).toInt,
        code = line(1).replaceAll("\"", ""),
        name = line(2).replaceAll("\"", ""),
        continent = line(3).replaceAll("\"", ""),
        wikipediaLink = line(4).replaceAll("\"", ""),
        keywords = if (line.length > 5) Some(line(5).replaceAll("\"", "")) else None
      )
    ) else None
}

object Airport {
  def from(line: Array[String]): Option[Airport] =
    if (line.length >= 14) {
      try {
        Some(Airport(
          id = line(0).filter(_.isDigit).toInt,
          ident = line(1).replaceAll("\"", ""),
          name = line(3).replaceAll("\"", ""),
          countryCode = line(8).replaceAll("\"", ""),
          latitude = line(4).replaceAll("\"", "").toDouble,
          longitude = line(5).replaceAll("\"", "").toDouble,
          elevation = Some(line(6).replaceAll("\"", "").toInt).filter(_ != 0)
        ))
      } catch {
        case _: NumberFormatException => None
      }
    } else None
}

object Runway {
  def from(line: Array[String]): Option[Runway] =
    if (line.length >= 9) {
      try {
        Some(Runway(
          id = line(0).filter(_.isDigit).toInt,
          airportRef = line(1).filter(_.isDigit).toInt,
          surface = line(5).replaceAll("\"", ""),
          leIdent = line(8).replaceAll("\"", ""),
          length = Some(line(3).replaceAll("\"", "").toInt).filter(_ != 0),
          width = Some(line(4).replaceAll("\"", "").toInt).filter(_ != 0),
          lighted = Some(line(6).replaceAll("\"", "").toInt),
          closed = Some(line(7).replaceAll("\"", "").toInt)
        ))
      } catch {
        case _: NumberFormatException => None
      }
    } else None
}