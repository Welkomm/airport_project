package models

class ModelsTest extends munit.FunSuite {
  // Country tests
  test("Country.from should parse valid CSV line") {
    val line = Array("1", "US", "United States", "NA", "http://example.com", "keywords")
    val result = Country.from(line)
    
    assertEquals(result.map(_.id), Some(1))
    assertEquals(result.map(_.code), Some("US"))
    assertEquals(result.map(_.name), Some("United States"))
    assertEquals(result.map(_.continent), Some("NA"))
    assertEquals(result.map(_.wikipediaLink), Some("http://example.com"))
    assertEquals(result.map(_.keywords), Some(Some("keywords")))
  }

  test("Country.from should handle missing optional fields") {
    val line = Array("1", "US", "United States", "NA", "http://example.com")
    val result = Country.from(line)
    
    assert(result.isDefined)
    assertEquals(result.flatMap(_.keywords), None)
  }

  // Airport tests
  test("Airport.from should parse valid CSV line") {
    val line = Array(
      "1", "KJFK", "type", "JFK Airport", 
      "40.6399", "-73.7787", "13", 
      "NA", "US"
    )
    val result = Airport.from(line)
    
    assertEquals(result.map(_.id), Some(1))
    assertEquals(result.map(_.ident), Some("KJFK"))
    assertEquals(result.map(_.name), Some("JFK Airport"))
    assertEquals(result.map(_.countryCode), Some("US"))
    assertEquals(result.map(_.latitude), Some(40.6399))
    assertEquals(result.map(_.longitude), Some(-73.7787))
    assertEquals(result.map(_.elevation), Some(Some(13)))
  }

  test("Airport.from should handle invalid numeric values") {
    val line = Array(
      "invalid", "KJFK", "type", "JFK Airport", 
      "invalid", "-73.7787", "13", 
      "NA", "US"
    )
    assertEquals(Airport.from(line), None)
  }

  // Runway tests
  test("Runway.from should parse valid CSV line") {
    val line = Array(
      "1", "123", "ident", "1000", "150",
      "ASPHALT", "1", "0", "09L"
    )
    val result = Runway.from(line)
    
    assertEquals(result.map(_.id), Some(1))
    assertEquals(result.map(_.airportRef), Some(123))
    assertEquals(result.map(_.surface), Some("ASPHALT"))
    assertEquals(result.map(_.leIdent), Some("09L"))
    assertEquals(result.map(_.length), Some(Some(1000)))
    assertEquals(result.map(_.width), Some(Some(150)))
    assertEquals(result.map(_.lighted), Some(Some(1)))
    assertEquals(result.map(_.closed), Some(Some(0)))
  }

  test("Runway.from should handle missing optional fields") {
    val line = Array(
      "1", "123", "ident", "", "",
      "ASPHALT", "", "", "09L"
    )
    val result = Runway.from(line)
    
    assert(result.isDefined)
    assertEquals(result.flatMap(_.length), None)
    assertEquals(result.flatMap(_.width), None)
    assertEquals(result.flatMap(_.lighted), None)
    assertEquals(result.flatMap(_.closed), None)
  }

  test("Runway.from should reject negative dimensions") {
    val line = Array(
      "1", "123", "ident", "-1000", "-150",
      "ASPHALT", "1", "0", "09L"
    )
    val result = Runway.from(line)
    
    assert(result.isDefined)
    assertEquals(result.flatMap(_.length), None)
    assertEquals(result.flatMap(_.width), None)
  }
}