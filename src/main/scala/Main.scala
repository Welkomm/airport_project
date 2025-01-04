import services.{CsvParser, DataStore}
import models.{Airport, Country, Runway}
import ui.{ConsoleUI, GuiApp}

@main 
def main(args: String*): Unit = {
  val isGuiMode = args.contains("--gui")

  if (isGuiMode) {
    GuiApp.main(Array.empty)
  } else {
    // Console UI mode
    val store = new DataStore()

    println("Loading data from CSV files...")

    // Load data with safe error handling
    val countries = CsvParser.parseCSV("resources/countries.csv")(Country.from)
    val airports = CsvParser.parseCSV("resources/airports.csv")(Airport.from)
    val runways = CsvParser.parseCSV("resources/runways.csv")(Runway.from)

    println(s"Loaded ${countries.size} countries")
    println(s"Loaded ${airports.size} airports")
    println(s"Loaded ${runways.size} runways")

    // Add data to store
    countries.foreach(store.addCountry)
    airports.foreach(store.addAirport)
    runways.foreach(store.addRunway)

    // Create and start the console UI
    val ui = new ConsoleUI(store)
    ui.start()
  }
}
