name                := "Akka HTTP Reactive Tweets Sandbox"
organization        := "karls"
version             := "1.0-SNAPSHOT"
scalaVersion        := "2.11.8"
scalacOptions       := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= Seq(
  "org.slf4j"           % "slf4j-simple"                         % "1.7.21",
  "com.typesafe.akka"  %% "akka-http"                            % "10.0.0",
  "com.typesafe.akka"  %% "akka-http-spray-json"                 % "10.0.0",
  "com.hunorkovacs"    %% "koauth"                               % "1.1.0"
)

lazy val tweets = project.in(file(".")).enablePlugins(GitBranchPrompt)

Revolver.settings
