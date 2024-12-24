package services

import scala.io.Source

object CsvParser {
  def parseCSV[T](filename: String, skipHeader: Boolean = true)
                 (parser: Array[String] => Option[T]): List[T] = {
    val source = Source.fromFile(filename, "UTF-8")
    try {
      val lines = source.getLines().toList
      val dataLines = if skipHeader then lines.tail else lines
      
      dataLines.flatMap { line => 
        val columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")
        parser(columns)
      }
    } finally {
      source.close()
    }
  }
}