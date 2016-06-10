name := "rentomatic"

version := "1.0"

lazy val `rentomatic` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  filters,
  "net.codingwell"    %% "scala-guice"            % "4.0.1",
  "com.iheart"        %% "play-swagger"           % "0.3.2-PLAY2.5",
  "org.webjars"       % "swagger-ui"              % "2.1.4",
  "com.typesafe.play" %% "play-slick"             % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions"  % "2.0.0",
  "com.h2database"    % "h2"                      % "1.4.191",
  specs2                                                            % Test,
  "org.specs2"        %% "specs2-matcher-extra"   % "3.6.6"         % Test
)

unmanagedResourceDirectories in Test <+= baseDirectory ( _ /"target/web/public/test" )

resolvers ++= Seq(
  "scalaz-bintray"            at "https://dl.bintray.com/scalaz/releases",
  "bintray-iheartradio-maven" at "http://dl.bintray.com/iheartradio/maven"
)

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Xmax-classfile-name", "128", // Support for encrypted Home directory
  "-language:experimental.macros" // Enable support for experimental macros for Mockito
)

coverageExcludedPackages := "controllers\\.Reverse.*;controllers\\.Reverse.*;controllers\\.javascript.*;router.*"

coverageMinimum := 90

coverageFailOnMinimum := true
