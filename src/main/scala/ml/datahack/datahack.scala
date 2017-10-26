package ml.datahack

import ml.core.Data
import edu.stanford.nlp.simple.Sentence
import scala.collection.JavaConverters._
import play.api.libs.json._
import scala.io.Source

case class Row(
  entity: String,
  disambig_term: String,
  text: String,
  url: String,
  articleName: Option[String])

case class RowWithText(
  entity: String,
  disambig_term: String,
  text: String,
  url: Option[String],
  wikiText: Option[String])

object DataHack {

  lazy val bot = new net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot("https://en.wikipedia.org/w/")

  def wikiText(article: String) = bot.getArticle(article).getText

  def wikiUrl(article: String) = s"https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro=&explaintext=&titles=$article"

  def wikiTextShort(article: String) = {
    val raw = Source.fromURL(wikiUrl(article)).getLines.toList.head
    val extract = Json.parse(raw) \\ "extract"
    extract.headOption.map(Json.stringify)
  }

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
    it.map {
      case Seq(entity, disambig_term, text, url) =>
        Row(
          entity,
          disambig_term,
          text,
          url,
          Option(url).map(_.split("/").last))
    }.toList
  }

  lazy val dataWithText = Data.extractCsv(Data.pathOf("withWikiShorts.csv"))("entity", "disambig_term", "text", "url", "articleName", "wikiText") { it =>
    it.map {
      case Seq(entity, disambig_term, text, url, _, wikiText) =>
        RowWithText(
          entity = entity,
          disambig_term = disambig_term,
          text = text,
          url = Option(url),
          wikiText = Option(wikiText))
    }.toList
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

    val sents = line.wikiText.map(sentences(_).toSeq).getOrElse(Seq.empty)
    JsObject(
      Seq(
        "entity" -> JsString(line.entity),
        "disambig_term" -> JsString(line.disambig_term),
        "text" -> Json.toJsObject(createHist(importantWords(line.text).map(_.toLowerCase))),
        "wikiText" -> Json.toJsObject(createHist(sents.flatMap(importantWords)))) ++
        line.url.map(x => "url" -> JsString(x)))
  }
}
