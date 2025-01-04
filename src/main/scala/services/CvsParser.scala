package services

import scala.io.Source

object CsvParser {
  // Méthode générique pour parser un fichier CSV 
  def parseCSV[T](filename: String, skipHeader: Boolean = true)
                 (parser: Array[String] => Option[T]): List[T] = {
    // Ouverture du fichier avec gestion des ressources
    val source = Source.fromFile(filename)
    // Conversion en liste de lignes
    val lines = source.getLines().toList 
    
    // Skip la première ligne si demandé
    val dataLines = if (skipHeader) lines.drop(1) else lines
    
    // Parse chaque ligne avec la fonction fournie
    val result = dataLines.flatMap { line => 
      // Split par virgules en gérant les champs entre guillemets 
      val columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")
      
      // Applique le parser et retourne une liste vide si échec
      parser(columns) match {
        case Some(parsed) => List(parsed)
        case None => Nil
      }
    }
    
    source.close()
    result
  }
}