import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

val zookeeperVersion = sys.env.get("ZOOKEEPER_VERSION").filter(_.nonEmpty).getOrElse("3.4.8")

val akkaVersion = "2.6.19"

val scala212Version = "2.12.13"
val scala213Version = "2.13.5"

val commonSettings = Seq(
  organization := "com.chatwork",
  homepage := Some(url("https://github.com/chatwork/reactive-zookeeper")),
  scalaVersion := scala213Version,
  crossScalaVersions := Seq(scala212Version, scala213Version),
  scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked", "-encoding", "UTF-8", "-language:implicitConversions", "-language:postfixOps"),
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
)

val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publish / skip := true,
  Compile / publishArtifact := false
)

val publishSettings = Seq(
  releaseCrossBuild := true,
  sonatypeProfileName := "com.chatwork",
  publishMavenStyle := true,
  publishTo := sonatypePublishToBundle.value,
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/chatwork/reactive-zookeeper"),
      "scm:git@github.com:chatwork/reactive-zookeeper.git"
    )
  ),
  developers := List(
    Developer(id="exoego", name="TATSUNO Yasuhiro", email="ytatsuno.jp@gmail.com", url=url("https://github.com/exoego"))
  ),
  versionScheme := Some("semver-spec"),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    releaseStepCommandAndRemaining("+publishSigned"),
    releaseStepCommand("sonatypeBundleRelease"),
    tagRelease,
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  credentials := {
    val ivyCredentials = (LocalRootProject / baseDirectory).value / ".credentials"
    val gpgCredentials = (LocalRootProject / baseDirectory).value / ".gpgCredentials"
    Credentials(ivyCredentials) :: Credentials(gpgCredentials) :: Nil
  },
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(publishSettings ++ noPublishSettings)
  .aggregate(reactiveZookeeper, reactiveZookeeperExample)

lazy val reactiveZookeeper = (project in file("reactive-zookeeper")).settings(
  name := "reactive-zookeeper",
  commonSettings ++ publishSettings ++ Seq(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.3",
      "org.apache.zookeeper" % "zookeeper" % zookeeperVersion % Provided,
      "org.slf4j" % "slf4j-log4j12" % "1.7.21" % Provided,
      "com.typesafe.akka" %% "akka-actor" % akkaVersion % Provided,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest-funsuite" % "3.2.7" % Test,
      "org.testng" % "testng" % "7.4.0" % Test,
      "org.apache.curator" % "curator-test" % "5.1.0" % Test
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
