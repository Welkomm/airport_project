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
  lighted: Option[Int], // Represents data as 1/0
  closed: Option[Int]   // Consistent with lighted field
)

object Country {
  def from(line: Array[String]): Option[Country] =
    if (line.length >= 5) {
      for {
        id <- line(0).toIntOption
      } yield Country(
        id = id,
        code = line(1).replaceAll("\"", ""),
        name = line(2).replaceAll("\"", ""),
        continent = line(3).replaceAll("\"", ""),
        wikipediaLink = line(4).replaceAll("\"", ""),
        keywords = line.lift(5).map(_.replaceAll("\"", ""))
      )
    } else {
      None
    }
}

object Airport {
  def from(line: Array[String]): Option[Airport] =
    if (line.length >= 9) {
      for {
        id <- line(0).toIntOption
        latitude <- line(4).toDoubleOption
        longitude <- line(5).toDoubleOption
      } yield Airport(
        id = id,
        ident = line(1).replaceAll("\"", ""),
        name = line(3).replaceAll("\"", ""),
        countryCode = line(8).replaceAll("\"", ""),
        latitude = latitude,
        longitude = longitude,
        elevation = line.lift(6).flatMap(_.toIntOption)
      )
    } else {
      None
    }
}

object Runway {
  def from(line: Array[String]): Option[Runway] =
    if (line.length >= 9) {
      for {
        id <- line(0).toIntOption
        airportRef <- line(1).toIntOption
      } yield Runway(
        id = id,
        airportRef = airportRef,
        surface = line(5).replaceAll("\"", ""),
        leIdent = line(8).replaceAll("\"", ""),
        length = line.lift(3).flatMap(_.toIntOption).filter(_ > 0),
        width = line.lift(4).flatMap(_.toIntOption).filter(_ > 0),
        lighted = line.lift(6).flatMap(_.toIntOption),
        closed = line.lift(7).flatMap(_.toIntOption)
      )
    } else {
      None
    }
}
