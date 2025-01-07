package ui

import services.DataStore
import models._

// Interface utilisateur en mode console
class ConsoleUI(store: DataStore) {

  def start(): Unit = {
    println("\nWelcome to the Airport Information System")
    println("1. Query")
    println("2. Reports")
    println("3. Exit")
    print("Select an option (1-3): ")

    val option = scala.io.StdIn.readLine()
    option match {
      case "1" => handleQuery()
      case "2" => handleReports()
      case "3" =>
        println("Exiting... Goodbye!")
      case _ =>
        println("Invalid option. Please try again.")
        start()
    }
  }

  // Gestion des requêtes de recherche par pays
  private def handleQuery(): Unit = {
    println("\nEnter country name or code: ")
    val query = scala.io.StdIn.readLine()

    store.getCountryByNameOrCode(query) match {
      case Some(country) =>
        println(s"\nCountry: ${country.name} (${country.code})")
        val airports = store.getAirportsForCountry(country.code)

        if (airports.isEmpty) {
          println("No airports found for this country.")
        } else {
          println(s"Found ${airports.size} airport(s):")
          // Construire le texte de sortie via map plutôt que foreach imbriqués
          val infoLines = airports.map { airport =>
            val runways = store.getRunwaysForAirport(airport.id)
            val runwayText =
              if (runways.isEmpty)
                "    No runways found for this airport."
              else {
                // Ici, pour éviter un foreach imbriqué, on peut faire un map -> mkString
                val lines = runways.map { r =>
                  s"      - ${r.leIdent} (Surface: ${r.surface})"
                }
                "    Runways:\n" + lines.mkString("\n")
              }
            s"\n  Airport: ${airport.name} (${airport.id})\n$runwayText"
          }
          println(infoLines.mkString("\n"))
        }

      case None =>
        println("Country not found. Please check your input and try again.")
    }
    start()
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
        handleReports()
    }
    start()
  }

  // Affichage des top 10 pays avec le plus/moins d'aéroports
  private def showAirportCounts(): Unit = {
    val topCountries = store.getCountriesWithMostAirports
    println("\nTop 10 countries with the most airports:")
    if (topCountries.isEmpty) {
      println("No data available.")
    } else {
      // pas de .head, .get => on utilise .map + mkString
      val lines = topCountries.map { case (country, count) =>
        s"  ${country.name} (${country.code}): $count airport(s)"
      }
      println(lines.mkString("\n"))
    }

    val bottomCountries = store.getCountriesWithFewestAirports
    println("\nTop 10 countries with the least airports:")
    if (bottomCountries.isEmpty) {
      println("No data available.")
    } else {
      val lines = bottomCountries.map { case (country, count) =>
        s"  ${country.name} (${country.code}): $count airport(s)"
      }
      println(lines.mkString("\n"))
    }
  }

  // Affichage des types de pistes par pays
  private def showRunwayTypes(): Unit = {
    val runwayTypes = store.getRunwayTypesPerCountry
    println("\nRunway types per country:")
    if (runwayTypes.isEmpty) {
      println("No data available.")
    } else {
      // Ici, on évite les .get en matchant correctement
      val lines = runwayTypes.toList.flatMap { case (code, surfaces) =>
        store.getCountryByNameOrCode(code).map { country =>
          val sList = surfaces.map(s => s"    - $s").mkString("\n")
          s"  ${country.name} (${country.code}):\n$sList"
        }
      }
      println(lines.mkString("\n\n"))
    }
  }

  // Affichage des latitudes de pistes les plus communes
  private def showCommonLatitudes(): Unit = {
    val commonLatitudes = store.getMostCommonRunwayLatitudes
    println("\nMost common runway latitudes:")
    if (commonLatitudes.isEmpty) {
      println("No data available.")
    } else {
      val lines = commonLatitudes.map { case (latitude, count) =>
        s"  $latitude: $count occurrence(s)"
      }
      println(lines.mkString("\n"))
    }
  }
}
