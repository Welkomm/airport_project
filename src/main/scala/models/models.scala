package models

object SafeParse:
  // Convertit une String en Option[Int] sans try/catch
  // On supprime les caractères non-chiffres, si c'est vide => None
  def safeToInt(raw: String): Option[Int] =
    val digits = raw.trim
    if digits.matches("^-?\\d+$") then
      // On vérifie manuellement si c'est un int
      // On peut faire un foldLeft pour convertir, ou un bigDecimal check
      // Pour simplifier, on fait un parse basé sur BigInt
      val maybeInt = bigIntToSafeInt(scala.util.Try(BigInt(digits)).toOption)
      maybeInt
    else None

  private def bigIntToSafeInt(biOpt: Option[BigInt]): Option[Int] =
    biOpt.flatMap { bi =>
      if bi.isValidInt then Some(bi.intValue)
      else None
    }

  // Convertit une String en Option[Double]
  def safeToDouble(raw: String): Option[Double] =
    val d = raw.trim
    if d.matches("^-?\\d+(\\.\\d+)?$") then
      // on tente un parse via BigDecimal => double
      scala.util.Try(BigDecimal(d)).toOption.map(_.toDouble)
    else None


case class Country(
  id: Int,
  code: String,
  name: String,
  continent: String,
  wikipediaLink: String,
  keywords: Option[String]
)

object Country:
  def from(line: Array[String]): Option[Country] =
    if line.length >= 5 then
      val idString = line(0).replaceAll("\"", "")
      SafeParse.safeToInt(idString).map { parsedId =>
        val code = line(1).replaceAll("\"", "")
        val name = line(2).replaceAll("\"", "")
        val continent = line(3).replaceAll("\"", "")
        val wiki = line(4).replaceAll("\"", "")
        val kws =
          if line.length > 5 then Some(line(5).replaceAll("\"", ""))
          else None
        Country(parsedId, code, name, continent, wiki, kws)
      }
    else None


case class Airport(
  id: Int,
  ident: String,
  name: String,
  countryCode: String,
  latitude: Double,
  longitude: Double,
  elevation: Option[Int]
)

object Airport:
  def from(line: Array[String]): Option[Airport] =
    if line.length >= 14 then
      val idStr  = line(0).replaceAll("\"", "")
      val latStr = line(4).replaceAll("\"", "")
      val lonStr = line(5).replaceAll("\"", "")
      val elevStr= line(6).replaceAll("\"", "")

      val maybeId   = SafeParse.safeToInt(idStr)
      val maybeLat  = SafeParse.safeToDouble(latStr)
      val maybeLon  = SafeParse.safeToDouble(lonStr)

      maybeId.flatMap { realId =>
        maybeLat.flatMap { realLat =>
          maybeLon.map { realLon =>
            val ident = line(1).replaceAll("\"", "")
            val name   = line(3).replaceAll("\"", "")
            val cCode  = line(8).replaceAll("\"", "")
            val elev   = SafeParse.safeToInt(elevStr).filter(_ != 0)
            Airport(realId, ident, name, cCode, realLat, realLon, elev)
          }
        }
      }
    else None


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

object Runway:
  def from(line: Array[String]): Option[Runway] =
    if line.length >= 9 then
      val idStr       = line(0).replaceAll("\"", "")
      val refStr      = line(1).replaceAll("\"", "")
      val lengthStr   = line(3).replaceAll("\"", "")
      val widthStr    = line(4).replaceAll("\"", "")
      val lightedStr  = line(6).replaceAll("\"", "")
      val closedStr   = line(7).replaceAll("\"", "")

      val maybeId  = SafeParse.safeToInt(idStr)
      val maybeRef = SafeParse.safeToInt(refStr)
      val maybeLen = SafeParse.safeToInt(lengthStr).filter(_ != 0)
      val maybeWid = SafeParse.safeToInt(widthStr).filter(_ != 0)
      val maybeLit = SafeParse.safeToInt(lightedStr)
      val maybeCl  = SafeParse.safeToInt(closedStr)

      maybeId.flatMap { realId =>
        maybeRef.map { realRef =>
          val surf = line(5).replaceAll("\"", "")
          val le   = line(8).replaceAll("\"", "")
          Runway(
            realId,
            realRef,
            surf,
            le,
            maybeLen,
            maybeWid,
            maybeLit,
            maybeCl
          )
        }
      }
    else None
