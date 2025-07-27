ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "3.7.1"

lazy val root = (project in file("."))
  .settings(
    name := "scala-zio2-https2-microservice",
    libraryDependencies ++= Dependencies.zio ++
      Dependencies.caliban ++
      Dependencies.logback ++
      Dependencies.pureconfig ++
      Dependencies.http4s ++
      Dependencies.circe ++
      Dependencies.doobie ++
      Dependencies.flyway ++
      Dependencies.ducktape,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions ++= Seq("-Ykind-projector:underscores","-Xmax-inlines:64"),
    assembly / assemblyDefaultJarName := "app.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.discard
      case string                                                     => MergeStrategy.defaultMergeStrategy(string)
    }
  )
