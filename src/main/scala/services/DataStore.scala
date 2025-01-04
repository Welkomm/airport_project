package services

import models._
import scala.collection.mutable

class DataStore {

  // Stockage en Map mutables (autorisé)
  private val countries = mutable.Map[String, Country]()
  private val airports  = mutable.Map[String, List[Airport]]()
  private val runways   = mutable.Map[Int, List[Runway]]()

  // Ajout
  def addCountry(country: Country): Unit =
    countries += (country.code -> country)

  def addAirport(airport: Airport): Unit =
    val oldList = airports.getOrElse(airport.countryCode, Nil)
    airports += (airport.countryCode -> (airport :: oldList))

  def addRunway(runway: Runway): Unit =
    val oldList = runways.getOrElse(runway.airportRef, Nil)
    runways += (runway.airportRef -> (runway :: oldList))


  // Recherche pays par code/nom + partial + fuzzy
  def getCountryByNameOrCode(query: String): Option[Country] =
    val normQ = query.toLowerCase

    // 1) exact
    val exact = countries.values.find { c =>
      c.code.toLowerCase == normQ || c.name.toLowerCase == normQ
    }
    exact match
      case Some(c) => Some(c)
      case None =>
        // 2) partial
        val partialList = countries.values.filter(c => c.name.toLowerCase.contains(normQ)).toList
        partialList match
          case c :: _ => Some(c)
          case Nil =>
            // 3) fuzzy => on cherche la plus petite distance
            val all = countries.values.toList
            val withDist = all.map { c =>
              (c, levenshteinDist(c.name.toLowerCase, normQ))
            }
            // minBy sans for
            val maybeMin = minByOption(withDist, (x: (Country, Int)) => x._2)
            maybeMin.flatMap { case (bestCountry, dist) =>
              if dist <= 3 then Some(bestCountry)
              else None
            }

  // minByOption => évite .minBy, et pas de for
  private def minByOption[A,B: Ordering](xs: List[A], f: A => B): Option[A] =
    xs match
      case Nil => None
      case x :: rest =>
        val best = rest.foldLeft(x) { (acc, cur) =>
          if implicitly[Ordering[B]].compare(f(cur), f(acc)) < 0 then cur else acc
        }
        Some(best)

  // Distance de Levenshtein **sans** boucle for => recursif + memo
  private val memo = mutable.Map.empty[(String,String), Int]

  private def levenshteinDist(a: String, b: String): Int =
    memo.getOrElseUpdate((a,b), {
      if a.isEmpty then b.length
      else if b.isEmpty then a.length
      else
        val cost = if a.charAt(0) == b.charAt(0) then 0 else 1
        val del  = 1 + levenshteinDist(a.drop(1), b)
        val ins  = 1 + levenshteinDist(a, b.drop(1))
        val sub  = cost + levenshteinDist(a.drop(1), b.drop(1))
        List(del, ins, sub).min
    })


  def getAirportsForCountry(countryCode: String): List[Airport] =
    airports.getOrElse(countryCode, Nil)

  def getRunwaysForAirport(airportId: Int): List[Runway] =
    runways.getOrElse(airportId, Nil)

  // Top 10 pays + d'aéroports
  def getCountriesWithMostAirports: List[(Country, Int)] =
    val all = countries.values.toList.map { c =>
      (c, airports.getOrElse(c.code, Nil).length)
    }
    // tri décroissant
    all.sortBy { case (_, count) => -count }.take(10)

  // Nouveau : top 10 pays - d'aéroports
  def getCountriesWithLeastAirports: List[(Country, Int)] =
    val all = countries.values.toList.map { c =>
      (c, airports.getOrElse(c.code, Nil).length)
    }
    // tri croissant
    all.sortBy { case (_, count) => count }.take(10)

  // Surfaces par pays
  def getRunwayTypesPerCountry: Map[String, Set[String]] =
    // pas de foreach imbriqué : on fait un .flatMap
    val pairs = countries.values.map { c =>
      val as = airports.getOrElse(c.code, Nil)
      val surfaces = as.flatMap { ap =>
        runways.getOrElse(ap.id, Nil).map(_.surface)
      }.toSet
      c.code -> surfaces
    }.toList
    pairs.toMap

  // Top 10 leIdent
  def getMostCommonRunwayLatitudes: List[(String, Int)] =
    val allRunways = runways.values.toList.flatten
    val grouped = allRunways.groupMapReduce(_.leIdent)(_ => 1)(_ + _)
    grouped.toList.sortBy { case (_, count) => -count }.take(10)
}
