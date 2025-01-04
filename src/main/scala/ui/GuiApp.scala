package ui

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import services.{CsvParser, DataStore}
import models._
import scala.compiletime.uninitialized

object GuiApp extends JFXApp3:

  private var searchField: TextField = uninitialized
  private var resultArea: TextArea   = uninitialized

  def start(): Unit =
    val store = new DataStore()

    // On charge
    CsvParser.parseCSV("resources/countries.csv")(Country.from).foreach(store.addCountry)
    CsvParser.parseCSV("resources/airports.csv")(Airport.from).foreach(store.addAirport)
    CsvParser.parseCSV("resources/runways.csv")(Runway.from).foreach(store.addRunway)

    stage = new JFXApp3.PrimaryStage:
      title = "Airport Information System"
      scene = new Scene(900, 800):
        searchField = new TextField:
          promptText = "Enter country name or code"
          prefWidth = 300

        resultArea = new TextArea:
          editable = false
          prefRowCount = 20
          prefColumnCount = 50
          wrapText = true

        root = new VBox(20):
          padding = Insets(20)
          alignment = Pos.Center

          children ++= Seq(
            new Label("Airport Information System"):
              style = "-fx-font-size: 24px;",

            new VBox(10):
              alignment = Pos.Center
              children ++= Seq(
                new Label("Search Country"),
                new HBox(10):
                  alignment = Pos.Center
                  children ++= Seq(
                    searchField,
                    new Button("Search"):
                      onAction = _ => handleSearch(store)
                  )
              ),

            new TitledPane:
              text = "Reports"
              content = new HBox(15):
                alignment = Pos.Center
                padding = Insets(10)
                children ++= Seq(
                  new Button("Airport Counts"):
                    onAction = _ => showAirportCounts(store),
                  new Button("Runway Types"):
                    onAction = _ => showRunwayTypes(store),
                  new Button("Runway Latitudes"):
                    onAction = _ => showCommonLatitudes(store)
                ),

            resultArea
          )

  private def handleSearch(store: DataStore): Unit =
    val countryOpt = store.getCountryByNameOrCode(searchField.text.value)
    countryOpt match
      case Some(country) =>
        val airports = store.getAirportsForCountry(country.code)
        val sb = new StringBuilder(s"Country: ${country.name}\n\n")
        airports.foreach { airport =>
          sb.append(s"Airport: ${airport.name}\n")
          val runways = store.getRunwaysForAirport(airport.id)
          runways.foreach { r =>
            sb.append(s"  Runway: ${r.leIdent} (${r.surface})\n")
          }
          sb.append("\n")
        }
        resultArea.text = sb.toString()

      case None =>
        resultArea.text = "Country not found"


  private def showAirportCounts(store: DataStore): Unit =
    val topCountries = store.getCountriesWithMostAirports
    val leastCountries = store.getCountriesWithLeastAirports

    val sb = new StringBuilder("Top 10 countries with most airports:\n\n")
    topCountries.foreach { case (country, count) =>
      sb.append(f"${country.name}%-30s $count airports\n")
    }

    sb.append("\nTop 10 countries with least airports:\n\n")
    leastCountries.foreach { case (country, count) =>
      sb.append(f"${country.name}%-30s $count airports\n")
    }

    resultArea.text = sb.toString()


  private def showRunwayTypes(store: DataStore): Unit =
    val runwayTypes = store.getRunwayTypesPerCountry
    val sb = new StringBuilder("Runway types per country:\n\n")

    runwayTypes.foreach { case (code, surfaces) =>
      store.getCountryByNameOrCode(code).foreach { c =>
        sb.append(f"${c.name}:\n")
        surfaces.foreach(surf => sb.append(f"  $surf\n"))
        sb.append("\n")
      }
    }

    resultArea.text = sb.toString()


  private def showCommonLatitudes(store: DataStore): Unit =
    val commonLatitudes = store.getMostCommonRunwayLatitudes
    val sb = new StringBuilder("Most common runway latitudes:\n\n")
    commonLatitudes.foreach { case (latitude, count) =>
      sb.append(f"$latitude%-20s $count occurrences\n")
    }
    resultArea.text = sb.toString()
