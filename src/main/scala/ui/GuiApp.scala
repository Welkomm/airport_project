package ui

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import services.{CsvParser, DataStore}
import models._

// Interface graphique utilisant ScalaFX
object GuiApp extends JFXApp3 {

  def start(): Unit = {
    // Initialisation du stockage de données
    val store = new DataStore()

    // Chargement des données depuis les fichiers CSV
    CsvParser.parseCSV("resources/countries.csv")(Country.from).foreach(store.addCountry)
    CsvParser.parseCSV("resources/airports.csv")(Airport.from).foreach(store.addAirport) 
    CsvParser.parseCSV("resources/runways.csv")(Runway.from).foreach(store.addRunway)

    // Configuration de la fenêtre principale
    stage = new JFXApp3.PrimaryStage {
      title = "Airport Information System"
      scene = new Scene(900, 800) {
        // Champ de recherche de pays
        val searchField = new TextField {
          promptText = "Enter country name or code"
          prefWidth = 300
        }

        // Zone d'affichage des résultats
        val resultArea = new TextArea {
          editable = false
          prefRowCount = 20
          prefColumnCount = 50
          wrapText = true
        }

        // Layout principal
        root = new VBox(20) {
          padding = Insets(20)
          alignment = Pos.Center

          children ++= Seq(
            // Titre
            new Label("Airport Information System") {
              style = "-fx-font-size: 24px;"
            },

            // Zone de recherche
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

            // Panneau des rapports
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

  // Gestion de la recherche de pays
  private def handleSearch(store: DataStore, searchField: TextField, resultArea: TextArea): Unit = {
    val countryOpt = store.getCountryByNameOrCode(searchField.text.value)
    countryOpt match {
      case Some(country) =>
        val airports = store.getAirportsForCountry(country.code)
        val sb = new StringBuilder(s"Country: ${country.name}\n\n")

        airports.foreach { airport =>
          sb.append(s"Airport: ${airport.name}\n")
          val runways = store.getRunwaysForAirport(airport.id)
          runways.foreach { runway =>
            sb.append(s"  Runway: ${runway.leIdent} (${runway.surface})\n")
          }
          sb.append("\n")
        }
        resultArea.text = sb.toString()

      case None =>
        resultArea.text = "Country not found"
    }
  }

  // Affichage des pays avec le plus d'aéroports
  private def topAirportCounts(store: DataStore, resultArea: TextArea): Unit = {
    val topCountries = store.getCountriesWithMostAirports
    val sb = new StringBuilder("Top 10 countries with most airports:\n\n")
    topCountries.foreach { case (country, count) =>
      sb.append(f"${country.name}%-30s $count airports\n")
    }
    resultArea.text = sb.toString()
  }

  // Affichage des pays avec le moins d'aéroports
  private def bottomAirportCounts(store: DataStore, resultArea: TextArea): Unit = {
    val bottomCountries = store.getCountriesWithFewestAirports
    val sb = new StringBuilder("Top 10 countries with fewest airports:\n\n")
    bottomCountries.foreach { case (country, count) =>
      sb.append(f"${country.name}%-30s $count airports\n")
    }
    resultArea.text = sb.toString()
  }

  // Affichage des types de pistes par pays
  private def showRunwayTypes(store: DataStore, resultArea: TextArea): Unit = {
    val runwayTypes = store.getRunwayTypesPerCountry
    val sb = new StringBuilder("Runway types per country:\n\n")
    runwayTypes.foreach { case (code, surfaces) =>
      store.getCountryByNameOrCode(code).foreach { country =>
        sb.append(f"${country.name}:\n")
        surfaces.foreach(surface => sb.append(f"  $surface\n"))
        sb.append("\n")
      }
    }
    resultArea.text = sb.toString()
  }

  // Affichage des latitudes de pistes les plus communes
  private def showCommonLatitudes(store: DataStore, resultArea: TextArea): Unit = {
    val commonLatitudes = store.getMostCommonRunwayLatitudes
    val sb = new StringBuilder("Most common runway latitudes:\n\n")
    commonLatitudes.foreach { case (latitude, count) =>
      sb.append(f"$latitude%-20s $count occurrences\n")
    }
    resultArea.text = sb.toString()
  }
}