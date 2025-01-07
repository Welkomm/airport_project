package test

import services.DataStore
import models.{Country, Airport, Runway}
import munit.FunSuite

class IntegrationTest extends FunSuite {

  val store = new DataStore()

  // Test data
  val testCountries = List(
    Country(1, "US", "United States", "NA", "link", None),
    Country(2, "FR", "France", "EU", "link", None)
  )
  
  val testAirports = List(
    Airport(1, "KJFK", "JFK", "US", -73.7787, 40.6399, Some(13)),
    Airport(2, "KLAX", "LAX", "US", -118.4081, 33.9425, Some(38)),
    Airport(3, "LFPG", "Charles de Gaulle", "FR", 2.5478, 49.0097, Some(119))
  )

  val testRunways = List(
    Runway(1, 1, "ASPHALT", "09L", Some(1000), Some(150), Some(1), Some(0)),
    Runway(2, 1, "CONCRETE", "27R", Some(2000), Some(150), Some(1), Some(0)),
    Runway(3, 2, "ASPHALT", "09L", Some(3000), Some(200), Some(1), Some(0)),
    Runway(4, 3, "CONCRETE", "08L", Some(4000), Some(200), Some(1), Some(0))
  )

  // Load test data into store
  testCountries.foreach(store.addCountry)
  testAirports.foreach(store.addAirport)
  testRunways.foreach(store.addRunway)

  test("country queries") {
    assertEquals(store.getCountryByNameOrCode("US"), Some(testCountries.head))
    assertEquals(store.getCountryByNameOrCode("France"), Some(testCountries(1)))
    assertEquals(store.getCountryByNameOrCode("XX"), None)
  }

  test("airport queries") {
    assertEquals(store.getAirportsForCountry("US").length, 2)
    assertEquals(store.getAirportsForCountry("FR").length, 1)
    assertEquals(store.getAirportsForCountry("XX"), Nil)
  }

  test("runway queries") {
    assertEquals(store.getRunwaysForAirport(1).length, 2)
    assertEquals(store.getRunwaysForAirport(2).length, 1)
    assertEquals(store.getRunwaysForAirport(99), Nil)
  }

  test("report generation - countries with most airports") {
    val topCountries = store.getCountriesWithMostAirports
    // On évite .head : on fait headOption
    topCountries.headOption match {
      case Some((firstCountry, count)) =>
        assertEquals(firstCountry.code, "US")
        assertEquals(count, 2)
      case None =>
        fail("No countries returned in topCountries")
    }
  }

  test("report generation - runway types per country") {
    val runwayTypes = store.getRunwayTypesPerCountry
    val usSurfaces  = runwayTypes.getOrElse("US", Set.empty)
    assert(usSurfaces.contains("ASPHALT"))
    assert(usSurfaces.contains("CONCRETE"))
  }

  test("report generation - most common runway identifiers") {
    val commonLatitudes = store.getMostCommonRunwayLatitudes
    val maybe09L = commonLatitudes.find(_._1 == "09L").map(_._2)
    assert(maybe09L.isDefined)
    assertEquals(maybe09L, Some(2))
  }

  test("countries with fewest airports") {
    val bottomCountries = store.getCountriesWithFewestAirports
    bottomCountries.headOption match {
      case Some((country, count)) =>
        assertEquals(country.code, "FR")
        assertEquals(count, 1)
      case None =>
        fail("No countries returned in bottomCountries")
    }
  }
}
