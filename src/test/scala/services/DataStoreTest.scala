package services

import models._
import munit.FunSuite

class DataStoreTest extends FunSuite {

  val dataStore = new DataStore()

  // Exemple de données de test
  val country1 = Country(1, "FR", "France", "Europe", "https://fr.wikipedia.org/wiki/France", Some("Keywords"))
  val country2 = Country(2, "US", "United States", "North America", "https://en.wikipedia.org/wiki/United_States", None)

  val airport1 = Airport(1, "CDG", "Paris Charles de Gaulle", "FR", 48.8566, 2.3522, Some(100))
  val airport2 = Airport(2, "LAX", "Los Angeles International", "US", 33.9416, -118.4085, Some(38))
  val airport3 = Airport(3, "NCE", "Nice Côte d'Azur", "FR", 43.665, 7.2167, Some(30))

  val runway1 = Runway(1, 1, "Concrete", "27L", Some(4000), Some(60), Some(1), Some(0))
  val runway2 = Runway(2, 2, "Asphalt", "25R", Some(3500), Some(50), Some(1), Some(1))
  val runway3 = Runway(3, 3, "Grass", "13", Some(3000), Some(45), Some(0), Some(0))

  // Test pour ajouter un pays
  test("addCountry should add a country to the data store") {
    dataStore.addCountry(country1)
    assertEquals(dataStore.getCountryByNameOrCode("FR"), Some(country1))
  }

  // Test pour ajouter un aéroport
  test("addAirport should add an airport to the data store") {
    dataStore.addAirport(airport1)
    assertEquals(dataStore.getAirportsForCountry("FR"), List(airport1))
  }

  // Test pour ajouter plusieurs aéroports dans le même pays
  test("addAirport should add multiple airports to the same country") {
    dataStore.addAirport(airport1)
    dataStore.addAirport(airport3)
    assertEquals(dataStore.getAirportsForCountry("FR"), List(airport3, airport1))
  }

  // Test pour ajouter une piste
  test("addRunway should add a runway to the data store") {
    dataStore.addRunway(runway1)
    assertEquals(dataStore.getRunwaysForAirport(1), List(runway1))
  }

  // Test pour ajouter plusieurs pistes à un même aéroport
  test("addRunway should add multiple runways to the same airport") {
    dataStore.addRunway(runway1)
    dataStore.addRunway(runway2)
    assertEquals(dataStore.getRunwaysForAirport(1), List(runway2, runway1))
  }

  // Test pour obtenir les pays avec le plus d'aéroports
  test("getCountriesWithMostAirports should return the countries with the most airports") {
    dataStore.addCountry(country1)
    dataStore.addCountry(country2)
    dataStore.addAirport(airport1)
    dataStore.addAirport(airport2)
    dataStore.addAirport(airport3)
    
    val result = dataStore.getCountriesWithMostAirports
    assertEquals(result, List((country1, 2), (country2, 1)))
  }

  // Test pour obtenir les pays avec le moins d'aéroports
  test("getCountriesWithFewestAirports should return the countries with the fewest airports") {
    dataStore.addCountry(country1)
    dataStore.addCountry(country2)
    dataStore.addAirport(airport1)
    dataStore.addAirport(airport2)
    dataStore.addAirport(airport3)
    
    val result = dataStore.getCountriesWithFewestAirports
    assertEquals(result, List((country2, 1), (country1, 2)))
  }

  // Test pour obtenir les types de pistes par pays
  test("getRunwayTypesPerCountry should return a map of runway types per country") {
    dataStore.addCountry(country1)
    dataStore.addCountry(country2)
    dataStore.addAirport(airport1)
    dataStore.addAirport(airport2)
    dataStore.addRunway(runway1)
    dataStore.addRunway(runway2)
    dataStore.addRunway(runway3)
    
    val result = dataStore.getRunwayTypesPerCountry
    assertEquals(result("FR"), Set("Concrete", "Asphalt"))
    assertEquals(result("US"), Set("Grass"))
  }

  // Test pour obtenir les latitudes les plus communes des pistes
  test("getMostCommonRunwayLatitudes should return the most common runway latitudes") {
    dataStore.addCountry(country1)
    dataStore.addAirport(airport1)
    dataStore.addRunway(runway1)
    dataStore.addRunway(runway2)
    dataStore.addRunway(runway3)

    val result = dataStore.getMostCommonRunwayLatitudes
    assertEquals(result.headOption.map(_._2), Some(1))
  }

  // Test pour la recherche exacte d'un pays par nom ou code
  test("getCountryByNameOrCode should find a country by exact name or code") {
    dataStore.addCountry(country1)
    dataStore.addCountry(country2)
    
    assertEquals(dataStore.getCountryByNameOrCode("FR"), Some(country1))
    assertEquals(dataStore.getCountryByNameOrCode("United States"), Some(country2))
  }

  // Test pour la recherche floue d'un pays par nom ou code
  test("getCountryByNameOrCode should find a country by fuzzy match") {
    dataStore.addCountry(country1)
    dataStore.addCountry(country2)
    
    assertEquals(dataStore.getCountryByNameOrCode("Fr"), Some(country1))  // fuzzy match
    assertEquals(dataStore.getCountryByNameOrCode("US"), Some(country2))  // exact match
  }
}
