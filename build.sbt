
val zookeeperVersion = "3.4.8"

val akkaVersion = "2.5.9"

val commonSettings = Seq(
  organization := "github.com/TanUkkii007",
  homepage := Some(url("https://github.com/TanUkkii007/reactive-zookeeper")),
  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.11.8", "2.12.4"),
  scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-encoding", "UTF-8", "-language:implicitConversions", "-language:postfixOps"),
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
)

val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publish / skip := true,
  Compile / publishArtifact := false,
  releaseCrossBuild := true
)

val publishSettings = Seq(
  releaseCrossBuild := true
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(reactiveZookeeper, reactiveZookeeperExample)

lazy val reactiveZookeeper = (project in file("reactive-zookeeper")).settings(
  name := "reactive-zookeeper",
  commonSettings ++ publishSettings ++ Seq(
    libraryDependencies ++= Seq(
      "org.apache.zookeeper" % "zookeeper" % zookeeperVersion % Provided,
      "org.slf4j" % "slf4j-log4j12" % "1.7.21" % Provided,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion % Provided,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.apache.curator" % "curator-test" % "2.11.0" % "test"
    )
  )
)

lazy val reactiveZookeeperExample = (project in file("example"))
  .settings(noPublishSettings)
  .settings(
  commonSettings ++ Seq(
    name := "reactive-zookeeper-example",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "org.apache.zookeeper" % "zookeeper" % zookeeperVersion
    )
  )
).dependsOn(reactiveZookeeper)
