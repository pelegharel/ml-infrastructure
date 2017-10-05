scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-csv" % "1.1",
  "org.apache.commons" % "commons-math3" % "3.6",
  "commons-lang" % "commons-lang" % "2.6",
  "net.java.truecommons" % "truecommons-shed" % "2.5.0",
  "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.13",
  "com.esotericsoftware" % "kryo" % "3.0.3",
  "com.univocity" % "univocity-parsers" % "2.0.0"
  )

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
