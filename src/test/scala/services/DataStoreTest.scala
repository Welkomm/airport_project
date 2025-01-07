import munit.FunSuite
import models._
import services._

class DataStoreTest extends FunSuite {

  test("addCountry should add a country to the datastore") {
    val datastore = new DataStore
    val country = Country(1, "FR", "France", "Europe", "https://fr.wikipedia.org", Some("Tourism"))

    datastore.addCountry(country)
    val result = datastore.getCountryByNameOrCode("FR")

    // Au lieu de .get, on pattern match
    result match {
      case Some(c) => assertEquals(c.name, "France")
      case None    => fail("Country not found")
    }
  }

  test("addAirport should add an airport to the datastore") {
    val datastore = new DataStore
    val country = Country(1, "FR", "France", "Europe", "https://fr.wikipedia.org", None)
    val airport = Airport(1, "CDG", "Charles de Gaulle", "FR", 49.0097, 2.5479, Some(392))

    datastore.addCountry(country)
    datastore.addAirport(airport)
    val result = datastore.getAirportsForCountry("FR")

    // On évite .head
    assertEquals(result.size, 1)
    result.headOption match {
      case Some(a) => assertEquals(a.name, "Charles de Gaulle")
      case None    => fail("Airport not found in country FR")
    }
  }

  test("addRunway should add a runway to the datastore") {
    val datastore = new DataStore
    val airport = Airport(1, "CDG", "Charles de Gaulle", "FR", 49.0097, 2.5479, Some(392))
    val runway  = Runway(1, 1, "Asphalt", "09L", Some(4000), Some(45), Some(1), Some(0))

    datastore.addAirport(airport)
    datastore.addRunway(runway)
    val result = datastore.getRunwaysForAirport(1)

    assertEquals(result.size, 1)
    result.headOption match {
      case Some(r) => assertEquals(r.surface, "Asphalt")
      case None    => fail("Runway not found for airport 1")
    }
  }

  test("getCountriesWithMostAirports should return top countries by airport count") {
    val datastore = new DataStore
    val country1 = Country(1, "FR", "France", "Europe", "https://fr.wikipedia.org", None)
    val country2 = Country(2, "US", "United States", "North America", "https://en.wikipedia.org", None)
    val airport1 = Airport(1, "CDG", "Charles de Gaulle", "FR", 49.0097, 2.5479, Some(392))
    val airport2 = Airport(2, "LAX", "Los Angeles", "US", 33.9416, -118.4085, Some(125))
    val airport3 = Airport(3, "JFK", "John F. Kennedy", "US", 40.6413, -73.7781, Some(13))

    datastore.addCountry(country1)
    datastore.addCountry(country2)
    datastore.addAirport(airport1)
    datastore.addAirport(airport2)
    datastore.addAirport(airport3)

    val result = datastore.getCountriesWithMostAirports
    // Au lieu de .head : headOption
    result.headOption match {
      case Some((firstCountry, count)) =>
        assertEquals(firstCountry.name, "United States")
        assertEquals(count, 2)
      case None =>
        fail("No countries returned")
    }
  }

  test("getRunwayTypesPerCountry should return runway types for each country") {
    val datastore = new DataStore
    val country = Country(1, "FR", "France", "Europe", "https://fr.wikipedia.org", None)
    val airport = Airport(1, "CDG", "Charles de Gaulle", "FR", 49.0097, 2.5479, Some(392))
    val runway1 = Runway(1, 1, "Asphalt", "09L", Some(4000), Some(45), Some(1), Some(0))
    val runway2 = Runway(2, 1, "Concrete", "27R", Some(3500), Some(60), Some(1), Some(0))

    datastore.addCountry(country)
    datastore.addAirport(airport)
    datastore.addRunway(runway1)
    datastore.addRunway(runway2)

    val result = datastore.getRunwayTypesPerCountry
    val surfaces = result.getOrElse("FR", Set.empty[String])

    assertEquals(surfaces, Set("Asphalt", "Concrete"))
  }
}
