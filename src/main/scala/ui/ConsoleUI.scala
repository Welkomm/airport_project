package ui

import services.DataStore
import models._

// Interface utilisateur en mode console
class ConsoleUI(store: DataStore) {
  // Menu principal avec options de base
  def start(): Unit = {
    println("\nWelcome to the Airport Information System")
    println("1. Query")
    println("2. Reports")
    println("3. Exit")
    print("Select an option (1-3): ")

    // Gestion des choix utilisateur
    val option = scala.io.StdIn.readLine()
    option match {
      case "1" => handleQuery()
      case "2" => handleReports()
      case "3" =>
        println("Exiting... Goodbye!")
      case _ =>
        println("Invalid option. Please try again.")
        start() // Recursion for retrying the input
    }
  }

  // Gestion des requêtes de recherche par pays
  private def handleQuery(): Unit = {
    println("\nEnter country name or code: ")
    val query = scala.io.StdIn.readLine()

    // Recherche et affichage des informations du pays
    store.getCountryByNameOrCode(query) match {
      case Some(country) =>
        println(s"\nCountry: ${country.name} (${country.code})")
        val airports = store.getAirportsForCountry(country.code)

        // Affichage des aéroports et pistes
        if (airports.isEmpty) {
          println("No airports found for this country.")
        } else {
          println(s"Found ${airports.size} airport(s):")
          airports.foreach { airport =>
            println(s"\n  Airport: ${airport.name} (${airport.id})")
            val runways = store.getRunwaysForAirport(airport.id)
            if (runways.isEmpty) {
              println("No runways found for this airport.")
            } else {
              println("    Runways:")
              runways.foreach { runway =>
                println(s"      - ${runway.leIdent} (Surface: ${runway.surface})")
              }
            }
          }
        }
      case None =>
        println("Country not found. Please check your input and try again.")
    }
    start() // Recursion for retrying the main menu
  }

  // Menu des rapports statistiques
  private def handleReports(): Unit = {
    println("\nReports:")
    println("1. Countries with the highest/lowest number of airports")
    println("2. Runway types per country")
    println("3. Most common runway latitudes")
    print("Select a report (1-3): ")

    val reportOption = scala.io.StdIn.readLine()
    reportOption match {
      case "1" => showAirportCounts()
      case "2" => showRunwayTypes()
      case "3" => showCommonLatitudes()
      case _ =>
        println("Invalid option. Please try again.")
        handleReports() // Recursion for retrying report selection
    }
    start() // Recursion for retrying the main menu
  }

  // Affichage des top 10 pays avec le plus/moins d'aéroports
  private def showAirportCounts(): Unit = {
    val topCountries = store.getCountriesWithMostAirports
    println("\nTop 10 countries with the most airports:")
    if (topCountries.isEmpty) {
      println("No data available.")
    } else {
      topCountries.foreach { case (country, count) =>
        println(s"  ${country.name} (${country.code}): $count airport(s)")
      }
    }

    val bottomCountries = store.getCountriesWithFewestAirports
    println("\nTop 10 countries with the least airports:")
    if (bottomCountries.isEmpty) {
      println("No data available.")
    } else {
      bottomCountries.foreach { case (country, count) =>
        println(s"  ${country.name} (${country.code}): $count airport(s)")
      }
    }
  }

  // Affichage des types de pistes par pays
  private def showRunwayTypes(): Unit = {
    val runwayTypes = store.getRunwayTypesPerCountry
    println("\nRunway types per country:")
    if (runwayTypes.isEmpty) {
      println("No data available.")
    } else {
      runwayTypes.foreach { case (code, surfaces) =>
        store.getCountryByNameOrCode(code) match {
          case Some(country) =>
            println(s"  ${country.name} (${country.code}):")
            surfaces.foreach(surface => println(s"    - $surface"))
          case None => // Do nothing if the country is not found
        }
      }
    }
  }

  // Affichage des latitudes de pistes les plus communes
  private def showCommonLatitudes(): Unit = {
    val commonLatitudes = store.getMostCommonRunwayLatitudes
    println("\nMost common runway latitudes:")
    if (commonLatitudes.isEmpty) {
      println("No data available.")
    } else {
      commonLatitudes.foreach { case (latitude, count) =>
        println(s"  $latitude: $count occurrence(s)")
      }
    }
  }
}
