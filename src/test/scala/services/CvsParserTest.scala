package services

import munit.FunSuite
import java.io.File

class CsvParserTest extends FunSuite {

  // Exemple de parser pour tester le CsvParser
  def testParser(columns: Array[String]): Option[String] = {
    if (columns.nonEmpty) Some(columns(0)) else None
  }

  test("parseCSV should correctly parse CSV data and return a list of results") {
    val filename = "test.csv"
    
    // Simuler un fichier CSV pour le test
    val testData = 
      """name,age,city
        |John,30,New York
        |Jane,25,Los Angeles
        |""".stripMargin

    // Écrire les données de test dans un fichier
    val writer = new java.io.PrintWriter(new File(filename))
    writer.write(testData)
    writer.close()

    // Appeler la méthode parseCSV
    val result = CsvParser.parseCSV(filename, skipHeader = false)(testParser)

    // Vérifier que le résultat est correct
    assertEquals(result, List("name", "John", "Jane"))

    // Supprimer le fichier après le test
    new File(filename).delete()
  }

  test("parseCSV should skip the header when skipHeader is true") {
    val filename = "test.csv"

    val testData = 
      """name,age,city
        |John,30,New York
        |Jane,25,Los Angeles
        |""".stripMargin

    // Écrire les données de test dans un fichier
    val writer = new java.io.PrintWriter(new File(filename))
    writer.write(testData)
    writer.close()

    // Appeler la méthode parseCSV avec skipHeader = true
    val result = CsvParser.parseCSV(filename, skipHeader = true)(testParser)

    // Vérifier que le résultat ne contient pas la première ligne (header)
    assertEquals(result, List("John", "Jane"))

    // Supprimer le fichier après le test
    new File(filename).delete()
  }

  test("parseCSV should not skip the header when skipHeader is false") {
    val filename = "test.csv"

    val testData = 
      """name,age,city
        |John,30,New York
        |Jane,25,Los Angeles
        |""".stripMargin

    // Écrire les données de test dans un fichier
    val writer = new java.io.PrintWriter(new File(filename))
    writer.write(testData)
    writer.close()

    // Appeler la méthode parseCSV avec skipHeader = false
    val result = CsvParser.parseCSV(filename, skipHeader = false)(testParser)

    // Vérifier que le résultat contient la première ligne (header)
    assertEquals(result, List("name", "John", "Jane"))

    // Supprimer le fichier après le test
    new File(filename).delete()
  }
}
