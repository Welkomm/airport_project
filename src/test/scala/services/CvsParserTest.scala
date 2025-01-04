package services

import scala.io.Source
import munit.FunSuite

class CvsParserTest extends FunSuite {

  test("parseCSV should parse lines with skipHeader") {
    // On peut juste tester la fonction, si besoin
    val testContent =
      """id,name
        |1,Test
        |2,Example
        |""".stripMargin

    val tempFile = java.io.File.createTempFile("testCsv", ".csv")
    val writer = new java.io.PrintWriter(tempFile)
    writer.write(testContent)
    writer.close()

    val list = CsvParser.parseCSV(tempFile.getAbsolutePath) { arr =>
      if arr.length >= 2 then Some(arr(1)) else None
    }

    assertEquals(list, List("Test", "Example"))

    tempFile.delete()
  }
}
