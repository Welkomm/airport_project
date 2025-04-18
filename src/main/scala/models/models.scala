package models

// Définition des classes de données (case classes) pour le modèle
case class Country(
  id: Int,                // Identifiant unique du pays
  code: String,           // Code ISO du pays
  name: String,           // Nom du pays 
  continent: String,      // Continent
  wikipediaLink: String,  // Lien Wikipedia
  keywords: Option[String] // Mots-clés optionnels
)

case class Airport(
  id: Int,                // Identifiant unique de l'aéroport
  ident: String,          // Code IATA/OACI
  name: String,           // Nom de l'aéroport
  countryCode: String,    // Code pays
  latitude: Double,       // Latitude
  longitude: Double,      // Longitude
  elevation: Option[Int]  // Altitude optionnelle
)

case class Runway(
  id: Int,                // Identifiant unique de la piste
  airportRef: Int,        // Référence à l'aéroport
  surface: String,        // Type de surface
  leIdent: String,        // Identifiant de la piste
  length: Option[Int],    // Longueur optionnelle
  width: Option[Int],     // Largeur optionnelle
  lighted: Option[Int],   // Éclairage (1/0)
  closed: Option[Int]     // Fermée (1/0)
)

// Objets compagnons pour la conversion depuis CSV
object Country {
  // Parse une ligne CSV en Country
  def from(line: Array[String]): Option[Country] = {
    if (line.length >= 5) {
      line(0).toIntOption.map { id =>
        Country(
          id           = id,
          code         = line(1).replaceAll("\"", ""),
          name         = line(2).replaceAll("\"", ""),
          continent    = line(3).replaceAll("\"", ""),
          wikipediaLink= line(4).replaceAll("\"", ""),
          keywords     = line.lift(5).map(_.replaceAll("\"", ""))
        )
      }
    } else None
  }
}

object Airport {
  def from(line: Array[String]): Option[Airport] = {
    if (line.length >= 9) {
      val maybeId        = line(0).toIntOption
      val maybeLatitude  = line(4).toDoubleOption
      val maybeLongitude = line(5).toDoubleOption

      maybeId.flatMap { id =>
        maybeLatitude.flatMap { latitude =>
          maybeLongitude.map { longitude =>
            Airport(
              id           = id,
              ident        = line(1).replaceAll("\"", ""),
              name         = line(3).replaceAll("\"", ""),
              countryCode  = line(8).replaceAll("\"", ""),
              latitude     = latitude,
              longitude    = longitude,
              elevation    = line.lift(6).flatMap(_.toIntOption)
            )
          }
        }
      }
    } else None
  }
}

object Runway {
  def from(line: Array[String]): Option[Runway] = {
    if (line.length >= 9) {
      val maybeId         = line(0).toIntOption
      val maybeAirportRef = line(1).toIntOption

      maybeId.flatMap { id =>
        maybeAirportRef.map { airportRef =>
          Runway(
            id         = id,
            airportRef = airportRef,
            surface    = line(5).replaceAll("\"", ""),
            leIdent    = line(8).replaceAll("\"", ""),
            length     = line.lift(3).flatMap(_.toIntOption).filter(_ > 0),
            width      = line.lift(4).flatMap(_.toIntOption).filter(_ > 0),
            lighted    = line.lift(6).flatMap(_.toIntOption),
            closed     = line.lift(7).flatMap(_.toIntOption)
          )
        }
      }
    } else None
  }
}
