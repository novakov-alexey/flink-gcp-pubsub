Global / onChangedBuildSource := ReloadOnSourceChanges

scalaVersion := "3.3.5"
lazy val flinkVersion = "1.18.1"
lazy val flinkVersionWithoutPatch =
  flinkVersion.split("\\.").take(2).mkString(".")

ThisBuild / libraryDependencies ++= Seq(
  "com.lihaoyi" %% "upickle" % "4.1.0",
  "org.apache.flink" % "flink-connector-gcp-pubsub" % s"3.1.0-$flinkVersionWithoutPatch",
  "org.flinkextended" %% "flink-scala-api" % s"${flinkVersion}_1.2.4",
  "org.apache.flink" % "flink-clients" % flinkVersion % Provided,
  "org.apache.flink" % "flink-runtime-web" % flinkVersion % Provided
)

ThisBuild / assemblyPackageScala / assembleArtifact := false

ThisBuild / assemblyMergeStrategy := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "module-info.class" =>
    MergeStrategy.first
  case "META-INF/io.netty.versions.properties" => MergeStrategy.first
  case "META-INF/module-info.class"            => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

Compile / run := Defaults
    .runTask(
      Compile / fullClasspath,
      Compile / run / mainClass,
      Compile / run / runner
    )
    .evaluated

Compile / run / fork := true