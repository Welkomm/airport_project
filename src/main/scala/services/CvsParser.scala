package services

import scala.io.Source

object CsvParser {
  def parseCSV[T](filename: String, skipHeader: Boolean = true)
                 (parser: Array[String] => Option[T]): List[T] = {
    // Pas de for ni var
    val source = Source.fromFile(filename)
    val lines = source.getLines().toList
    source.close()

    val dataLines = 
      if skipHeader && lines.nonEmpty then lines.tail
      else lines

    // flatMap => pas de boucle for
    dataLines.flatMap { line =>
      val columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")
      parser(columns)
    }
  }
}
