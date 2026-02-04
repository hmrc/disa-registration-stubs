ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.5"

lazy val microservice = Project("disa-registration-stubs", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions += "-Wconf:src=routes/.*:s"
  )
  .settings(CodeCoverageSettings.settings *)
  .settings(PlayKeys.playDefaultPort := 1203)

addCommandAlias("prePrChecks", "scalafmtCheckAll;scalafmtSbtCheck")

addCommandAlias("precommit", ";scalafmtAll;scalafmtSbt;coverage;test;it/test;coverageReport")

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")
