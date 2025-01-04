package models

class ModelsTest extends munit.FunSuite {

  test("Country.from should parse valid CSV line") {
    val line = Array("1", "US", "United States", "NA", "http://example.com", "keywords")
    val result = Country.from(line)

    assertEquals(result.map(_.id), Some(1))
    assertEquals(result.map(_.code), Some("US"))
    assertEquals(result.map(_.name), Some("United States"))
  }

  test("Airport.from should parse valid CSV line") {
    val line = Array(
      "1",       // id
      "KJFK",    // ident
      "airport", // type
      "JFK",     // name
      "40.6399", // latitude
      "-73.7787",// longitude
      "13",      // elevation
      "NA",      // continent
      "US",      // country_code
      "NY",      // region
      "New York",// municipality
      "1",       // scheduled_service
      "GPS",     // gps_code
      "JFK"      // iata_code
    )
    val result = Airport.from(line)

    assertEquals(result.map(_.id), Some(1))
    assertEquals(result.map(_.ident), Some("KJFK"))
    assertEquals(result.map(_.name), Some("JFK"))
    assertEquals(result.map(_.countryCode), Some("US"))
  }

  test("Runway.from should parse valid CSV line") {
    val line = Array(
      "1",    // id
      "123",  // airport_ref
      "000",  // airport_ident
      "1000", // length_ft
      "150",  // width_ft
      "ASPHALT", // surface
      "1",    // lighted
      "0",    // closed
      "09L"   // le_ident
    )
    val result = Runway.from(line)

    assertEquals(result.map(_.id), Some(1))
    assertEquals(result.map(_.airportRef), Some(123))
    assertEquals(result.map(_.surface), Some("ASPHALT"))
    assertEquals(result.map(_.leIdent), Some("09L"))
  }
}
