package services

import munit.FunSuite
import models._

class DataStoreTest extends FunSuite {

  // On crée un fixture qui réinitialise un DataStore avant chaque test
  val storeFixture = FunFixture[DataStore](
    setup = { _ => new DataStore() },
    teardown = { _ => () }
  )

  // Test data
  val testCountry = Country(1, "US", "United States", "NA", "link", None)
  val testAirport = Airport(1, "KJFK", "JFK", "US", 40.6399, -73.7787, Some(13))
  val testRunway  = Runway(1, 1, "ASPHALT", "09L", Some(1000), Some(150), Some(1), Some(0))

  storeFixture.test("should store and retrieve country by code") { store =>
    store.addCountry(testCountry)
    assertEquals(store.getCountryByNameOrCode("US"), Some(testCountry))
  }

  storeFixture.test("should store and retrieve country by name") { store =>
    store.addCountry(testCountry)
    assertEquals(store.getCountryByNameOrCode("United States"), Some(testCountry))
  }

  storeFixture.test("should return None for non-existent country") { store =>
    assertEquals(store.getCountryByNameOrCode("XX"), None)
  }

  storeFixture.test("should store and retrieve airports for country") { store =>
    store.addAirport(testAirport)
    assertEquals(store.getAirportsForCountry("US"), List(testAirport))
  }

  storeFixture.test("should return empty list for country with no airports") { store =>
    assertEquals(store.getAirportsForCountry("XX"), Nil)
  }

  storeFixture.test("should store and retrieve runways for airport") { store =>
    store.addRunway(testRunway)
    assertEquals(store.getRunwaysForAirport(1), List(testRunway))
  }

  storeFixture.test("should return empty list for non-existent airport runways") { store =>
    assertEquals(store.getRunwaysForAirport(99), Nil)
  }

  storeFixture.test("should get countries with most airports sorted by count") { store =>
    val countries = List(
      Country(1, "US", "United States", "NA", "link", None),
      Country(2, "FR", "France", "EU", "link", None)
    )
    val airports = List(
      Airport(1, "KJFK", "JFK", "US", 40.6399, -73.7787, Some(13)),
      Airport(2, "KLAX", "LAX", "US", 33.9425, -118.4081, Some(38)),
      Airport(3, "LFPG", "CDG", "FR", 49.0097, 2.5478, Some(119))
    )

    countries.foreach(store.addCountry)
    airports.foreach(store.addAirport)

    val result = store.getCountriesWithMostAirports
    assertEquals(result.head._1.code, "US")
    assertEquals(result.head._2, 2)
    assert(result.length <= 10)
  }
}
