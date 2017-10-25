package ml.datahack

import ml.core.Data

case class Row(
  entity: String,
  disambig_term: String,
  text: String,
  url: String,
  articleName: Option[String])

object DataHack {

  lazy val bot = new net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot("https://en.wikipedia.org/w/")

  def wikiText(article: String) = bot.getArticle(article).getText

  def words(text: String) = {
    val wordFilters = Seq('{', '\\', '<', '>', '_')
    val lines = text.split("\n").map(_.split(" "))
    lines.map {
      line =>
        line.
          filterNot(w => wordFilters.exists(w.contains(_))).
          map(_.filter(_.isLetter).toLowerCase).
          filterNot(_.isEmpty)
    }.filterNot(_.isEmpty).
      flatten.
      groupBy(identity).
      mapValues(_.length)
  }

  lazy val data = Data.extractCsv(Data.pathOf("train.csv"))("entity", "disambig_term", "text", "wikipedia_link") { it =>
    it.
      take(6080).
      map {
        case Seq(entity, disambig_term, text, url) =>
          Row(
            entity,
            disambig_term,
            text,
            url,
            Option(url).map(_.split("/").last))
      }.
      toList
  }
}