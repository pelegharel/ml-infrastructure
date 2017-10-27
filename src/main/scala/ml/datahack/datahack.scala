package ml.datahack

import ml.core.Data
import edu.stanford.nlp.simple.Sentence
import scala.collection.JavaConverters._
import play.api.libs.json._
import scala.io.Source
import scala.util.Try
import scala.concurrent.duration._
import scala.concurrent.{ Future, Await }

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

  def wikiUrl(articleRaw: String) = {
    val article = java.net.URLEncoder.encode(articleRaw, java.nio.charset.StandardCharsets.UTF_8.toString)
    s"https://en.wikipedia.org/w/api.php?format=json&action=query&maxlag=600&prop=extracts&exintro=&explaintext=&titles=$article"
  }

  def wikiTextShort(article: Seq[String]) = {
    val src = Source.fromURL(wikiUrl(article.mkString("|")))
    val raw = src.getLines.toList.head
    src.close()
    Try {
      val extract = Json.parse(raw)
      val JsObject(aaa) = extract("query")("pages")
      aaa.values.flatMap(x => Try(Json.stringify(x("title")) -> Json.stringify(x("extract"))).toOption).toMap.
        map { case (k, v) => k.replace(" ", "_").replace("\"", "") -> v }
    }.toOption.getOrElse(Map.empty)
  }

  def saveWikiText(it: Iterator[LabaledTrain], saveName: String) = {
    val withText = it.grouped(20).flatMap { l =>
      val extractedText = wikiTextShort(l.map(_.article_name))
      println(extractedText.size)
      l.map(x => x -> extractedText.get(x.article_name))
    }.map {
      case (row, txt) => row.productIterator.toSeq :+ txt.getOrElse(null)
    }

    Data.writeCsv(Data.pathOf(saveName))(

      "entity", "disambig_term", "text", "article_name", "real_article", "label", "wikiTest")(withText.toIterator)
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
      map(_._2.filter(_.isLetter)).
      filterNot(_.isEmpty)
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

  lazy val dataWithText = Data.extractCsv(Data.pathOf("withWikiShorts.csv"))(
    "entity",
    "disambig_term",
    "text",
    "url",
    "articleName",
    "wikiText") { it =>
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

  def saveHistCsv(saveName: String, it: Iterator[(LabaledTrain, Option[String])]) = {
    val writer = Data.writeCsv(Data.pathOf(saveName))(
      "entity", "disambig_term", "text", "article_name", "real_article", "label",
      "textHist",
      "wikiHist")(
        it.map {
          case (r, txt) =>
            val (textHist, wikiHist) =
              extractWordHist(Sample(r.entity, r.disambig_term, r.text, r.article_name, txt))
            val textHistJ = Json.toJson(textHist)
            val wikiHistJ = Json.toJson(wikiHist)
            r.productIterator.toSeq ++ Seq(textHistJ, wikiHistJ)
        })
  }

  lazy val definitions = Data.extractCsv(Data.pathOf("dis.csv"))("entity", "definition") {
    it =>
      it.toSeq.view.
        map { case Seq(a, b) => (a, b) }.
        groupBy(_._1).
        mapValues(_.map(_._2))
  }

  def enrichRow(row: Row) =
    definitions.get(row.disambig_term).map {
      _.map { d =>
        (d, row, row.articleName.filter(_ == d).isDefined)
      }
    }.getOrElse(Seq.empty)

  lazy val testAsRows = {
    Data.extractCsv(Data.pathOf("testData.csv"))("entity", "disambig_term", "text") {
      it =>
        it.map {
          case Seq(entity, disambig_term, text) =>
            Row(
              entity = entity,
              disambig_term = disambig_term,
              text = text,
              url = "",
              articleName = None)
        }.toList

    }
  }
  def getEnrichedData(saveName: String, rows: Iterator[Row]) = {
    val withLabel = rows.flatMap(enrichRow)
    val saveData = withLabel.map {
      case (term, row, label) =>
        Seq(row.entity, row.disambig_term, row.text, term, row.articleName.getOrElse(null), label)
    }
    Data.writeCsv(Data.pathOf(saveName))("entity", "disambig_term", "text", "article_name", "real_article", "label")(saveData)
  }

  case class Sample(entity: String, disambig_term: String, text: String, article_name: String, wikiText: Option[String])
  def extractWordHist(sample: Sample) = {
    val removeTerms = Set(sample.entity, sample.disambig_term)
    val wikiText = sample.wikiText
    val scentenseWords = createHist(importantWords(sample.text).map(_.toLowerCase), removeTerms)

    val sents = wikiText.map(sentences(_).toSeq).getOrElse(Seq.empty)
    val wikiWords = createHist(sents.flatMap(importantWords), removeTerms)
    (scentenseWords, wikiWords)
  }

  case class LabaledTrain(entity: String, disambig_term: String, text: String, article_name: String, real_article: Option[String], label: Boolean)

  def labeledData(file: String = "labeledTrain.csv") = Data.extractCsv(Data.pathOf(file))("entity", "disambig_term", "text", "article_name", "real_article", "label") {
    it =>
      it.map {
        case Seq(entity, disambig_term, text, article_name, real_article, label) =>
          LabaledTrain(entity, disambig_term, text, article_name, Option(real_article), label.toBoolean)
      }.toList
  }

  lazy val labeledWithWiki = Data.extractCsv(Data.pathOf("labeled_wiki_text.csv"))("entity", "disambig_term", "text", "article_name", "real_article", "label", "wikiText") {
    it =>
      it.map {
        case Seq(entity, disambig_term, text, article_name, real_article, label, wikiText) =>
          LabaledTrain(entity, disambig_term, text, article_name, Option(real_article), label.toBoolean) -> Option(wikiText)
      }.toList
  }
  def createHist(words: Seq[String], removeTerms: Set[String]) = {
    words.
      groupBy(identity).
      mapValues(_.length).
      filterKeys(k =>
        !(removeTerms.
          map(_.toLowerCase).
          contains(k)))
  }

  def extract(line: RowWithText) = {
    def createHist(words: Seq[String], removeTerms: Set[String]) = {
      Option(words).map(_.
        groupBy(identity).
        mapValues(_.length).
        filterKeys(k =>
          !(removeTerms.
            map(_.toLowerCase).
            contains(k)))).getOrElse(Map.empty)
    }

    val sents = line.wikiText.map(sentences(_).toSeq).getOrElse(Seq.empty)
    JsObject(
      Seq(
        "entity" -> JsString(line.entity),
        "disambig_term" -> JsString(line.disambig_term),
        "text" -> Json.toJsObject(createHist(importantWords(line.text).map(_.toLowerCase), Set(line.entity, line.disambig_term))),
        "wikiText" -> Json.toJsObject(createHist(sents.flatMap(importantWords), Set(line.entity, line.disambig_term)))) ++
        line.url.map(x => "url" -> JsString(x)))
  }
}
