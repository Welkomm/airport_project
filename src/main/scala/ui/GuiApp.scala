package ui

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import services.{CsvParser, DataStore}
import models._

object GuiApp extends JFXApp3 {

  def start(): Unit = {
    val store = new DataStore()

    // Chargement des données
    CsvParser.parseCSV("resources/countries.csv")(Country.from).foreach(store.addCountry)
    CsvParser.parseCSV("resources/airports.csv")(Airport.from).foreach(store.addAirport) 
    CsvParser.parseCSV("resources/runways.csv")(Runway.from).foreach(store.addRunway)

    stage = new JFXApp3.PrimaryStage {
      title = "Airport Information System"
      scene = new Scene(900, 800) {
        val searchField = new TextField {
          promptText = "Enter country name or code"
          prefWidth = 300
        }

        val resultArea = new TextArea {
          editable = false
          prefRowCount = 20
          prefColumnCount = 50
          wrapText = true
        }

        root = new VBox(20) {
          padding = Insets(20)
          alignment = Pos.Center

          children ++= Seq(
            new Label("Airport Information System") {
              style = "-fx-font-size: 24px;"
            },
            new VBox(10) {
              alignment = Pos.Center
              children ++= Seq(
                new Label("Search Country"),
                new HBox(10) {
                  alignment = Pos.Center
                  children ++= Seq(
                    searchField,
                    new Button("Search") {
                      onAction = _ => handleSearch(store, searchField, resultArea)
                    }
                  )
                }
              )
            },
            new TitledPane {
              text = "Reports"
              content = new HBox(15) {
                alignment = Pos.Center
                padding = Insets(10)
                children ++= Seq(
                  new Button("Top 10 Countries with Most Airports") {
                    onAction = _ => topAirportCounts(store, resultArea)
                  },
                  new Button("Top 10 Countries with Fewest Airports") {
                    onAction = _ => bottomAirportCounts(store, resultArea)
                  },
                  new Button("Runway Types") {
                    onAction = _ => showRunwayTypes(store, resultArea)
                  },
                  new Button("Runway Latitudes") {
                    onAction = _ => showCommonLatitudes(store, resultArea)
                  }
                )
              }
            },
            resultArea
          )
        }
      }
    }
  }

  private def handleSearch(store: DataStore, searchField: TextField, resultArea: TextArea): Unit = {
    val countryOpt = store.getCountryByNameOrCode(searchField.text.value)
    val sb = new StringBuilder

    countryOpt match {
      case Some(country) =>
        val airports = store.getAirportsForCountry(country.code)
        sb.append(s"Country: ${country.name}\n\n")

        if (airports.isEmpty) {
          sb.append("No airports found.\n")
        } else {
          airports.foreach { airport =>
            sb.append(s"Airport: ${airport.name}\n")
            val runways = store.getRunwaysForAirport(airport.id)
            if (runways.isEmpty) {
              sb.append("  No runways found.\n\n")
            } else {
              val runwaysInfo = runways.map(r => s"    - ${r.leIdent} (${r.surface})").mkString("\n")
              sb.append("  Runways:\n")
              sb.append(runwaysInfo + "\n\n")
            }
          }
        }
      case None =>
        sb.append("Country not found")
    }
    resultArea.text = sb.toString()
  }

  private def topAirportCounts(store: DataStore, resultArea: TextArea): Unit = {
    val topCountries = store.getCountriesWithMostAirports
    val sb = new StringBuilder("Top 10 countries with most airports:\n\n")
    if (topCountries.nonEmpty) {
      topCountries.foreach { case (country, count) =>
        sb.append(f"${country.name}%-30s $count airports\n")
      }
    } else {
      sb.append("No data available.\n")
    }
    resultArea.text = sb.toString()
  }

  private def bottomAirportCounts(store: DataStore, resultArea: TextArea): Unit = {
    val bottomCountries = store.getCountriesWithFewestAirports
    val sb = new StringBuilder("Top 10 countries with fewest airports:\n\n")
    if (bottomCountries.nonEmpty) {
      bottomCountries.foreach { case (country, count) =>
        sb.append(f"${country.name}%-30s $count airports\n")
      }
    } else {
      sb.append("No data available.\n")
    }
    resultArea.text = sb.toString()
  }

  private def showRunwayTypes(store: DataStore, resultArea: TextArea): Unit = {
    val runwayTypes = store.getRunwayTypesPerCountry
    val sb = new StringBuilder("Runway types per country:\n\n")
    if (runwayTypes.nonEmpty) {
      runwayTypes.foreach { case (code, surfaces) =>
        store.getCountryByNameOrCode(code).foreach { country =>
          sb.append(s"${country.name}:\n")
          surfaces.foreach(surface => sb.append(s"  $surface\n"))
          sb.append("\n")
        }
      }
    } else {
      sb.append("No data available.\n")
    }
    resultArea.text = sb.toString()
  }

  private def showCommonLatitudes(store: DataStore, resultArea: TextArea): Unit = {
    val commonLatitudes = store.getMostCommonRunwayLatitudes
    val sb = new StringBuilder("Most common runway latitudes:\n\n")
    if (commonLatitudes.nonEmpty) {
      commonLatitudes.foreach { case (latitude, count) =>
        sb.append(f"$latitude%-20s $count occurrences\n")
      }
    } else {
      sb.append("No data available.\n")
    }
    resultArea.text = sb.toString()
  }
}