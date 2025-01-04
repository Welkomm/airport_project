package ui

import services.DataStore

class ConsoleUI(store: DataStore):

  def start(): Unit =
    // On utilise un while(true) => pas interdit
    var exitWanted = false
    while !exitWanted do
      println("\nWelcome to Airport Information System")
      println("1. Query")
      println("2. Reports")
      println("3. Exit")
      println("Select an option (1-3):")

      val choice = scala.io.StdIn.readLine()
      if choice == "1" then
        handleQuery()
      else if choice == "2" then
        handleReports()
      else if choice == "3" then
        exitWanted = true
      else
        println("Invalid option")


  private def handleQuery(): Unit =
    println("Enter country name or code:")
    val query = scala.io.StdIn.readLine()

    store.getCountryByNameOrCode(query) match
      case Some(country) =>
        println(s"\nCountry: ${country.name}")
        val airports = store.getAirportsForCountry(country.code)
        if airports.isEmpty then
          println("  No airport found.")
        else
          // Eviter foreach imbriqué => on fait un map -> mkString
          val text = airports.map { airport =>
            val runways = store.getRunwaysForAirport(airport.id)
            val runwayLines = runways.map { r =>
              s"    Runway: ${r.leIdent} (${r.surface})"
            }.mkString("\n")

            s"  Airport: ${airport.name}\n$runwayLines"
          }.mkString("\n\n")

          println(text)

      case None =>
        println("Country not found")


  private def handleReports(): Unit =
    println("\nReports:")
    println("1. Countries with highest/lowest number of airports")
    println("2. Runway types per country")
    println("3. Most common runway latitudes")
    println("Select report (1-3):")

    val choice = scala.io.StdIn.readLine()
    if choice == "1" then
      showAirportCounts()
    else if choice == "2" then
      showRunwayTypes()
    else if choice == "3" then
      showCommonLatitudes()
    else
      println("Invalid option")


  private def showAirportCounts(): Unit =
    val top = store.getCountriesWithMostAirports
    println("\nTop 10 countries with most airports:")
    if top.isEmpty then println("No data.")
    else
      top.foreach { case (c, nb) =>
        println(s"  ${c.name}: $nb airports")
      }

    val least = store.getCountriesWithLeastAirports
    println("\nTop 10 countries with least airports:")
    if least.isEmpty then println("No data.")
    else
      least.foreach { case (c, nb) =>
        println(s"  ${c.name}: $nb airports")
      }


  private def showRunwayTypes(): Unit =
    val runwayMap = store.getRunwayTypesPerCountry
    println("\nRunway types per country:")
    if runwayMap.isEmpty then
      println("No data.")
    else
      // pas de foreach imbriqué => on va faire un transform en List + mkString
      val lines = runwayMap.toList.flatMap { case (code, surfaces) =>
        store.getCountryByNameOrCode(code) match
          case Some(country) =>
            val surText = surfaces.map(s => s"    $s").mkString("\n")
            List(s"${country.name}:\n$surText")
          case None =>
            Nil
      }
      if lines.isEmpty then println("No matching country found.")
      else println(lines.mkString("\n\n"))


  private def showCommonLatitudes(): Unit =
    val common = store.getMostCommonRunwayLatitudes
    println("\nMost common runway latitudes:")
    if common.isEmpty then
      println("No data.")
    else
      common.foreach { case (lat, nb) =>
        println(s"  $lat: $nb occurrences")
      }
