scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-csv" % "1.1",
  "org.apache.commons" % "commons-math3" % "3.6",
  "commons-lang" % "commons-lang" % "2.6",
  "net.java.truecommons" % "truecommons-shed" % "2.5.0",
  "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.13",
  "com.esotericsoftware" % "kryo" % "3.0.3",
  "com.univocity" % "univocity-parsers" % "2.0.0",
  "net.sourceforge" % "jwbf" % "3.1.1",
  "com.atlassian.commonmark" % "commonmark" % "0.10.0",
  "org.jsoup" % "jsoup" % "1.10.3",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.8.0" classifier "models"
  )

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
