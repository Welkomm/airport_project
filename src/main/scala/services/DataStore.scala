package services

import models._
import scala.collection.mutable

class DataStore {
  // Maps mutables pour stocker les données
  private val countries = mutable.Map[String, Country]()        // Stocke pays par code
  private val airports = mutable.Map[String, List[Airport]]()   // Stocke aéroports par code pays
  private val runways = mutable.Map[Int, List[Runway]]()       // Stocke pistes par id aéroport

  // Ajoute un pays à la map
  def addCountry(country: Country): Unit =
    countries += (country.code -> country)

  // Ajoute un aéroport à la liste des aéroports du pays
  def addAirport(airport: Airport): Unit = {
    val currentAirports = airports.get(airport.countryCode) match {
      case Some(list) => airport :: list
      case None => List(airport)
    }
    airports += (airport.countryCode -> currentAirports)
  }

  // Ajoute une piste à la liste des pistes de l'aéroport 
  def addRunway(runway: Runway): Unit = {
    val currentRunways = runways.get(runway.airportRef) match {
      case Some(list) => runway :: list
      case None => List(runway)
    }
    runways += (runway.airportRef -> currentRunways)
  }

  // Recherche un pays par nom ou code avec correspondance floue
  def getCountryByNameOrCode(query: String): Option[Country] = {
    val normalizedQuery = query.toLowerCase

    def exactMatch = countries.values.find(country =>
      country.code.toLowerCase == normalizedQuery ||
      country.name.toLowerCase == normalizedQuery
    )

    def partialMatch = countries.values.find(_.name.toLowerCase.contains(normalizedQuery))

    def fuzzyMatch = countries.values
      .map(country => country -> levenshteinDistance(country.name.toLowerCase, normalizedQuery))
      .filter(_._2 <= 3)
      .toList
      .sortBy(_._2)
      .headOption
      .map(_._1)

    exactMatch.orElse(partialMatch).orElse(fuzzyMatch)
  }

  // Algorithme de Levenshtein pour la recherche floue
  private def levenshteinDistance(s1: String, s2: String): Int = {
    val dist = Array.ofDim[Int](s1.length + 1, s2.length + 1)
    
    dist(0) = (0 to s2.length).toArray
    (0 to s1.length).map(i => dist(i)(0) = i)
    
    val indices = for {
      i <- 1 to s1.length
      j <- 1 to s2.length
    } yield (i, j)
    
    indices.map { case (i, j) =>
      val cost = if (s1(i - 1) == s2(j - 1)) 0 else 1
      dist(i)(j) = List(
        dist(i - 1)(j) + 1,
        dist(i)(j - 1) + 1,
        dist(i - 1)(j - 1) + cost
      ).min
    }
    
    dist(s1.length)(s2.length)
  }

  // Retourne les aéroports d'un pays
  def getAirportsForCountry(countryCode: String): List[Airport] =
    airports.get(countryCode).getOrElse(List.empty[Airport])

  // Retourne les pistes d'un aéroport
  def getRunwaysForAirport(airportId: Int): List[Runway] =
    runways.get(airportId).getOrElse(List.empty[Runway])

  // Top 10 pays avec le plus d'aéroports 
  def getCountriesWithMostAirports: List[(Country, Int)] =
    countries.values
      .map(country => 
        country -> airports.get(country.code).map(_.size).getOrElse(0))
      .toList
      .sortBy(-_._2)
      .take(10)

  // Top 10 pays avec le moins d'aéroports
  def getCountriesWithFewestAirports: List[(Country, Int)] =
    countries.values  
      .map(country =>
        country -> airports.get(country.code).map(_.size).getOrElse(0))
      .toList
      .sortBy(_._2)
      .take(10)

  // Types de pistes par pays
  def getRunwayTypesPerCountry: Map[String, Set[String]] =
    countries.values.map { country =>
      val surfaces = for {
        airportList <- airports.get(country.code).toList
        airport <- airportList
        runwayList <- runways.get(airport.id).toList
        runway <- runwayList
      } yield runway.surface
      country.code -> surfaces.toSet
    }.toMap

  // Top 10 des identifiants de pistes les plus communs
  def getMostCommonRunwayLatitudes: List[(String, Int)] =
    runways.values.flatten
      .groupMapReduce(_.leIdent)(_ => 1)(_ + _)
      .toList
      .sortBy(-_._2)
      .take(10)
}