package ml.datahack

import ml.core.Data

case class Row(
  entity: String,
  disambig_term: String,
  text: String,
  articleName: Option[String])

object DataHack {

  lazy val bot = new net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot("https://en.wikipedia.org/w/")
  lazy val data = Data.extractCsv(Data.pathOf("train.csv"))("entity", "disambig_term", "text", "wikipedia_link") { it =>
    it.
      take(6080).
      map {
        case Seq(entity, disambig_term, text, url) =>
          Row(
            entity,
            disambig_term,
            text,
            Option(url).map(_.split("/").last))
      }.
      toList
  }
}