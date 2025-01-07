package services

import models._
import scala.collection.mutable

class DataStore {
  // Maps mutables pour stocker les données
  private val countries = mutable.Map[String, Country]()     // Stocke pays par code
  private val airports  = mutable.Map[String, List[Airport]]() // Stocke aéroports par code pays
  private val runways   = mutable.Map[Int, List[Runway]]()  // Stocke pistes par id aéroport

  // Ajoute un pays
  def addCountry(country: Country): Unit =
    countries += (country.code -> country)

  // Ajoute un aéroport
  def addAirport(airport: Airport): Unit = {
    val currentAirports = airports.getOrElse(airport.countryCode, Nil)
    airports += (airport.countryCode -> (airport :: currentAirports))
  }

  // Ajoute une piste
  def addRunway(runway: Runway): Unit = {
    val currentRunways = runways.getOrElse(runway.airportRef, Nil)
    runways += (runway.airportRef -> (runway :: currentRunways))
  }

  // Recherche un pays (nom ou code) avec correspondance floue
  def getCountryByNameOrCode(query: String): Option[Country] = {
    val normalizedQuery = query.toLowerCase

    val exactMatch = countries.values.find { country =>
      country.code.toLowerCase == normalizedQuery ||
      country.name.toLowerCase == normalizedQuery
    }

    lazy val partialMatch = countries.values.find { country =>
      country.name.toLowerCase.contains(normalizedQuery)
    }

    // On calcule en différé un fuzzy match
    lazy val fuzzyMatch: Option[Country] = {
      // Calcule la distance pour tous les pays
      val distances = countries.values.map { c =>
        (c, levenshteinDistance(c.name.toLowerCase, normalizedQuery))
      }
      // On retient ceux dont la distance <= 3
      val possibleMatches = distances.filter(_._2 <= 3).toList
      // On trie par distance et prend le premier
      possibleMatches.sortBy(_._2).headOption.map(_._1)
    }

    exactMatch.orElse(partialMatch).orElse(fuzzyMatch)
  }

  // Algorithme de Levenshtein sans for
  private def levenshteinDistance(s1: String, s2: String): Int = {
    val lenS1 = s1.length
    val lenS2 = s2.length
    val dist  = Array.ofDim[Int](lenS1 + 1, lenS2 + 1)

    // Initialisation
    (0 to lenS1).foreach(i => dist(i)(0) = i)
    (0 to lenS2).foreach(j => dist(0)(j) = j)

    // Remplissage sans for
    def fill(iList: List[Int]): Unit = iList match {
      case Nil => ()
      case i :: xs =>
        def fillJ(jList: List[Int]): Unit = jList match {
          case Nil => ()
          case j :: ys =>
            val cost = if (s1.charAt(i - 1) == s2.charAt(j - 1)) 0 else 1
            val del  = dist(i - 1)(j) + 1
            val ins  = dist(i)(j - 1) + 1
            val sub  = dist(i - 1)(j - 1) + cost
            dist(i)(j) = List(del, ins, sub).min
            fillJ(ys)
        }
        fillJ((1 to lenS2).toList)
        fill(xs)
    }

    fill((1 to lenS1).toList)
    dist(lenS1)(lenS2)
  }

  // Retourne la liste d'aéroports d'un pays
  def getAirportsForCountry(countryCode: String): List[Airport] =
    airports.getOrElse(countryCode, Nil)

  // Retourne la liste de pistes d'un aéroport
  def getRunwaysForAirport(airportId: Int): List[Runway] =
    runways.getOrElse(airportId, Nil)

  // Top 10 pays avec le plus d'aéroports
  def getCountriesWithMostAirports: List[(Country, Int)] = {
    val listAll = countries.values.map { country =>
      val count = airports.get(country.code).map(_.size).getOrElse(0)
      (country, count)
    }.toList
    listAll.sortBy(-_._2).take(10)
  }

  // Top 10 pays avec le moins d'aéroports
  def getCountriesWithFewestAirports: List[(Country, Int)] = {
    val listAll = countries.values.map { country =>
      val count = airports.get(country.code).map(_.size).getOrElse(0)
      (country, count)
    }.toList
    listAll.sortBy(_._2).take(10)
  }

  // Types de pistes par pays (sans for comprehension)
  def getRunwayTypesPerCountry: Map[String, Set[String]] = {
    // On va transformer la Map pays -> surfaces
    countries.values.foldLeft(Map.empty[String, Set[String]]) { (acc, country) =>
      val surfacesAll =
        airports.getOrElse(country.code, Nil).flatMap { airport =>
          runways.getOrElse(airport.id, Nil).map(_.surface)
        }
      acc + (country.code -> surfacesAll.toSet)
    }
  }

  // Top 10 des identifiants de pistes
  def getMostCommonRunwayLatitudes: List[(String, Int)] = {
    val leIdCount = runways.values.flatten.foldLeft(Map.empty[String, Int]) { (acc, runway) =>
      val key  = runway.leIdent
      val prev = acc.getOrElse(key, 0)
      acc + (key -> (prev + 1))
    }
    leIdCount.toList.sortBy(-_._2).take(10)
  }
}
