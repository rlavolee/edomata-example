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

ThisBuild / scalaVersion := "2.13.10"

lazy val root = tlCrossRootProject.aggregate(domain, catsEffect)

lazy val domain = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .enablePlugins(NoPublishPlugin)
  .settings(
    name := "edomata-domain-example",
    libraryDependencies ++= Seq(
      ("dev.hnaderi" %%% "edomata-skunk-circe" % "0.10.1").cross(CrossVersion.for2_13Use3),
      ("dev.hnaderi" %%% "edomata-munit" % "0.10.1").cross(CrossVersion.for2_13Use3) % Test,
      ("io.circe" %%% "circe-generic" % "0.14.5").cross(CrossVersion.for2_13Use3)
    )
  )

lazy val catsEffect = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .dependsOn(domain)
  .enablePlugins(NoPublishPlugin)
  .settings(
    name := "edomata-ce-example"
  )

ThisBuild / scalacOptions +="-Ytasty-reader"
