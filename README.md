# Airport Project

This repository contains a Scala/SBT project that parses three CSV files (countries, airports, and runways) and provides:

1. **A Console Application** for querying countries and generating various reports.
2. **An Optional ScalaFX GUI** (launched via `--gui`) that provides a graphical interface for the same features.

---

## Table of Contents

- [Overview](#overview)  
- [Project Structure](#project-structure)  
- [Build and Run](#build-and-run)  
- [Usage](#usage)  
- [Features](#features)  
- [Tests](#tests)  
- [Requirements and Constraints](#requirements-and-constraints)  
- [Contributing](#contributing)  
- [License](#license)

---

## Overview

- **Language & Framework**: Scala 3, managed by SBT.
- **CSV Parsing**: Custom CSV splitting using regex. **No external library** for CSV parsing.
- **Data Storage**: An in-memory `DataStore` simulates a database, storing `Country`, `Airport`, and `Runway` data.
- **Reports** include:
  - The 10 countries with the highest number of airports.
  - The 10 countries with the lowest number of airports.
  - Runway surfaces listed by country.
  - The top 10 most common runway identifiers (`le_ident`).
- **Fuzzy Matching**: Country searches include partial and Levenshtein-based matching (e.g., `"zimb"` finds `"Zimbabwe"`).

---

## Project Structure

```plaintext
.
├── build.sbt
├── project
│   └── build.properties
└── src
    ├── main
    │   └── scala
    │       ├── Main.scala
    │       ├── models
    │       │   └── models.scala
    │       ├── services
    │       │   ├── CvsParser.scala
    │       │   └── DataStore.scala
    │       └── ui
    │           ├── ConsoleUI.scala
    │           └── GuiApp.scala
    └── test
        └── scala
            ├── IntegrationTest.scala
            ├── models
            │   └── ModelsTest.scala
            └── services
                ├── CvsParserTest.scala
                └── DataStoreTest.scala
```

- **`Main.scala`**: Application entry point.  
- **`models`**: Defines `Country`, `Airport`, `Runway` and their `.from` methods for CSV parsing.  
- **`services`**:
  - `CsvParser` for reading CSV files.
  - `DataStore` for storing/querying countries, airports, runways.
- **`ui`**:
  - `ConsoleUI` for the command-line interface.
  - `GuiApp` for the ScalaFX graphical interface.

---

## Build and Run

1. **Install** [SBT](https://www.scala-sbt.org/download.html) (version 1.10.5 or higher) and ensure you have a **Java 8+** JDK.
2. **Clone** this repository or download the source code.
3. **Open a terminal** in the project root directory.

### Running in Console Mode

```bash
sbt run
```

- This launches the console application with a simple menu:
  - **`1`** for querying a country by code or name (supports partial/fuzzy).
  - **`2`** for reports (top/bottom countries, runway types, runway identifiers).
  - **`3`** to exit.

### Running in GUI Mode

```bash
sbt "run --gui"
```

- This opens a ScalaFX-based window to let you search for countries and generate the same reports via buttons.

---

## Usage

### Console Interface

After `sbt run`, you will see:

1. **Query**: Enter a country name/code (partial matches allowed). You’ll see its airports and each airport’s runways.
2. **Reports**:
   - **Top 10** countries with the **most** airports.
   - **Top 10** countries with the **least** airports.
   - **Runway types** grouped by country (surface column).
   - **Top 10** most common runway identifiers (`le_ident`).
3. **Exit**: Closes the application.

### GUI Interface

When run with `--gui`, you can:

- **Search** for a country in the text field and press **Search**. Results display in a text area.
- **View Reports** by clicking **Airport Counts**, **Runway Types**, or **Runway Latitudes**. Results display in the text area.

---

## Features

- **CSV Parsing** without external dependencies.
- **Fuzzy Matching** (Levenshtein distance) for partial country name queries (e.g., searching `"zim"` → `"Zimbabwe"`).
- **Flexible Reports** on airports and runways (top/bottom countries, runway surfaces, etc.).
- **Tests**: Uses MUnit for integration and unit tests.

---

## Tests

We use [MUnit](https://scalameta.org/munit/) for testing:

- **IntegrationTest**: Verifies end-to-end functionality (adding data and querying).
- **ModelsTest**: Checks that CSV lines parse into correct `Country`, `Airport`, `Runway` objects.
- **DataStoreTest**: Ensures in-memory queries and data storage behave correctly.
- **CvsParserTest**: (optional) tests CSV parsing logic on a small sample file.

Run tests via:

```bash
sbt test
```

---

## Requirements and Constraints

- **Scala 3** (see `build.sbt`).
- **No usage** of:
  - `try`, `catch`, `throw`
  - `for` loops
  - `var`, `null`, `return`
  - `.get` on `Option`, or `head`
  - Nested `foreach`
- **Mutable Maps** in `DataStore` are allowed only as a database replacement.
- **No external library** for CSV parsing; we rely on `Source.fromFile` + manual `split(...)`.

---

## Contributing

1. **Fork** the repo and **create a new branch** for your changes.
2. Avoid the restricted keywords (`for`, `try`, `var`, `null`, etc.); keep the code purely functional or use methods like `map`, `flatMap`, `foldLeft`.
3. **Commit** and push your branch, then open a **Pull Request**.

---

## License

This project is provided as-is. No explicit license is attached. If you plan to distribute or use it publicly, please check with relevant stakeholders or add a license of your choice.

---

**Download this README**  
To download, simply copy the content above into a file named `README.md` in your project directory.
