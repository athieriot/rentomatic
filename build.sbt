name := "rentomatic"

version := "1.0"

lazy val `rentomatic` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  "net.codingwell" %% "scala-guice" % "4.0.1",
  "com.iheart" %% "play-swagger" % "0.2.1-PLAY2.5",
  "org.webjars" % "swagger-ui" % "2.1.4",
  specs2 % Test,
  "org.specs2" %% "specs2-matcher-extra" % "3.6.6" % "test"
)

unmanagedResourceDirectories in Test <+= baseDirectory ( _ /"target/web/public/test" )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

coverageExcludedPackages := "controllers\\.Reverse.*;controllers\\.javascript.*;router.*"

coverageMinimum := 80

coverageFailOnMinimum := true