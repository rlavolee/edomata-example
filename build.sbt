// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.0" // your current series x.y

ThisBuild / organization := "dev.hnaderi"
ThisBuild / organizationName := "Hossein Naderi"
ThisBuild / startYear := Some(2023)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("hnaderi", "Hossein Naderi")
)

ThisBuild / scalaVersion := "3.2.2"

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .enablePlugins(NoPublishPlugin)
  .settings(
    name := "edomata-example",
    libraryDependencies ++= Seq(
      "dev.hnaderi" %%% "edomata-skunk-circe" % "0.9.1",
      "dev.hnaderi" %%% "edomata-munit" % "0.9.1" % Test,
      "io.circe" %%% "circe-generic" % "0.14.3"
    )
  )
