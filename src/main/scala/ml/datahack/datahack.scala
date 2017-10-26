package ml.datahack

import ml.core.Data
import edu.stanford.nlp.simple.Sentence
import scala.collection.JavaConverters._
import play.api.libs.json.Json

case class Row(
  entity: String,
  disambig_term: String,
  text: String,
  url: String,
  articleName: Option[String])

case class RowWithText(
  entity: String,
  disambig_term: String,
  articleName: String,
  text: String,
  url: String,
  wikiText: String)

object DataHack {

  lazy val bot = new net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot("https://en.wikipedia.org/w/")

  def wikiText(article: String) = bot.getArticle(article).getText

  def sentences(text: String) = {
    val wordFilters = Seq('{', '\\', '<', '>', '_')
    val lines = text.
      split('.').
      flatMap(_.split("\n")).
      map(_.split(" "))

    lines.map {
      line =>
        line.
          filterNot(w => wordFilters.exists(w.contains(_))).
          map(_.filter(_.isLetter).toLowerCase).
          filterNot(_.isEmpty)
    }.
      map(_.mkString(" ")).
      filterNot(_ matches " *")
  }

  def importantWords(sentence: String) = {
    val badTags = Set("CC", "DT", "IN", "PRP", "PRP$", "TO", "WDT", "WP", "WP$", "WRB", "RB", "VBD", "VBZ")

    val s = new Sentence(sentence)
    s.posTags.asScala.zip(s.words.asScala).
      filterNot {
        case (tag, _) =>
          badTags.contains(tag)
      }.
      map(_._2)
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

  def extract(line: RowWithText) = {
    def createHist(words: Seq[String]) = {
      Option(words).map(_.
        groupBy(identity).
        mapValues(_.length).
        filterKeys(k =>
          !(Set(line.entity, line.disambig_term).
            map(_.toLowerCase).
            contains(k)))).getOrElse(Map.empty)
    }

    val sents = Option(line.wikiText).map(sentences(_).toSeq).getOrElse(Seq.empty)
    Map(
      "entity" -> line.entity,
      "disambig_term" -> line.disambig_term,
      "articleName" -> line.articleName,
      "url" -> line.url,
      "text" -> createHist(importantWords(line.text)),
      "wikiText" -> createHist(sents.flatMap(importantWords)))
  }
}
