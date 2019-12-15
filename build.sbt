import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

ThisBuild / javacOptions ++= Seq("-source", "13")

lazy val root = (project in file("."))
  .settings(  
    name := "sbt-with-jdk13-example",

    // disable javadoc/scaladoc generation
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,

      // https://mvnrepository.com/artifact/net.logstash.logback/logstash-logback-encoder
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "6.3",
    
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.10.1",

    // https://mvnrepository.com/artifact/com.tersesystems.logback/logback-structured-config
    resolvers += Resolver.bintrayRepo("tersesystems", "maven"),
    libraryDependencies += "com.tersesystems.logback" % "logback-structured-config" % "0.13.1",

    //-------------------------------------
    // Docker settings
    //-------------------------------------

    // Always use latest tag
    dockerUpdateLatest := true,

    // https://registry.hub.docker.com/r/azul/zulu-openjdk
    // Remember to use AshScriptPlugin if you are using an alpine based image like zulu-openjdk-alpine
    dockerBaseImage := "azul/zulu-openjdk:13",

    // If you want to publish to a remote docker repository, uncomment the following:
    //dockerRepository := Some("remote-docker-hostname"),
    //Docker / packageName := "remote-repository-name",

    // If we're running in a docker container, then export logging volume.
    Docker / defaultLinuxLogsLocation := "/opt/docker/logs",
    dockerExposedVolumes := Seq((Docker / defaultLinuxLogsLocation).value),
    dockerEnvVars := Map(
      "LOG_DIR" -> (Docker / defaultLinuxLogsLocation).value,
    ),

    //-------------------------------------
    // Test packaging settings
    //-------------------------------------

    fork in Test := true,
    javaOptions in test ++= Seq(
      "-Dlocal.logback.environment=test",
      "-Dlog.dir=test-logs"
    ),

    //-------------------------------------
    // "Universal" packaging settings that apply to everything
    //-------------------------------------

    // bashScriptExtraDefines is useful for passing through JDK flight recorder options etc, depending on environment.
    // https://sbt-native-packager.readthedocs.io/en/stable/recipes/package_configuration.html#sbt-parameters-and-build-environment
    //
    // https://sbt-native-packager.readthedocs.io/en/stable/archetypes/java_app/customize.html#bashscript-defines
    // Pass through the logback environment -- if dockerEnvVar set one then use that, otherwise default to production.
    bashScriptExtraDefines += """addJava "-Dlocal.logback.environment=${LOGBACK_ENVIRONMENT:-production}"""",
    bashScriptExtraDefines += """addJava "-Dlog.dir=${LOG_DIR:-${app_home}/../logs}"""",

    javaOptions in Universal ++= Seq(
      "-J-Xmx512m",
      "-J-Xms512m",
      "-J-XX:+UnlockExperimentalVMOptions",
      "-J-XX:+UseZGC",
    ),

    libraryDependencies += scalaTest % Test
  ).enablePlugins(DockerPlugin, JavaAppPackaging)

