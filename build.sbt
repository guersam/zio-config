import BuildHelper._

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://zio.dev/zio-config/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "afsalthaj",
        "Afsal Thaj",
        "https://medium.com/@afsal.taj06",
        url("https://github.com/afsalthaj")
      ),
      Developer(
        "jdegoes",
        "John De Goes",
        "john@degoes.net",
        url("http://degoes.net")
      )
    )
  )
)

addCommandAlias("fmt", "; scalafmtSbt; scalafmt; test:scalafmt")
addCommandAlias("fix", "; all compile:scalafix test:scalafix; all scalafmtSbt scalafmtAll")
addCommandAlias("compileAll", "; ++2.12.16; root2-12/compile; ++2.13.8!; root2-13/compile")
addCommandAlias("testAll", "; ++2.12.16; root2-12/test; ++2.13.8!; root2-13/test")
addCommandAlias(
  "testJS",
  ";zioConfigJS/test"
)

addCommandAlias(
  "testJVM212",
  ";zioConfigJVM/test;zioConfigTypesafeJVM/test;zioConfigDerivationJVM/test;zioConfigYamlJVM/test;examplesJVM/test;zioConfigAwsJVM/test;zioConfigZioAwsJVM/test;zioConfigXmlJVM/test"
)
addCommandAlias(
  "testJVM213",
  ";zioConfigJVM/test;zioConfigTypesafeJVM/test;zioConfigDerivationJVM/test;zioConfigYamlJVM/test;zioConfigRefinedJVM/test;zioConfigMagnoliaJVM/test;examplesJVM/test;zioConfigTypesafeMagnoliaTestsJVM/test;zioConfigAwsJVM/test;zioConfigZioAwsJVM/test;zioConfigXmlJVM/test"
)
addCommandAlias(
  "testJVM3x",
  ";zioConfigJVM/test;zioConfigTypesafeJVM/test;zioConfigDerivationJVM/test;zioConfigYamlJVM/test;zioConfigAwsJVM/test;zioConfigZioAwsJVM/test;zioConfigXmlJVM/test"
)

val awsVersion        = "1.12.360"
val zioAwsVersion     = "5.19.8.4"
val zioVersion        = "2.0.9"
val magnoliaVersion   = "0.17.0"
val refinedVersion    = "0.10.1"
val pureconfigVersion = "0.16.0"
val shapelessVersion  = "2.4.0-M1"

lazy val magnoliaDependencies =
  libraryDependencies ++= {
    if (scalaBinaryVersion.value == "2.11" || scalaVersion.value == ScalaDotty) Seq.empty // Just to make IntelliJ happy
    else {
      Seq(
        "com.propensive" %% "magnolia"      % magnoliaVersion,
        "org.scala-lang"  % "scala-reflect" % scalaVersion.value
      )
    }
  }

lazy val refinedDependencies =
  libraryDependencies ++= {
    if (scalaBinaryVersion.value == "2.11") Seq.empty // Just to make IntelliJ happy
    else Seq("eu.timepit" %% "refined" % refinedVersion)
  }

lazy val pureconfigDependencies =
  libraryDependencies ++= {
    if (scalaBinaryVersion.value == "2.11" || scalaVersion.value == ScalaDotty) Seq.empty // Just to make IntelliJ happy
    else Seq("com.github.pureconfig" %% "pureconfig" % pureconfigVersion)
  }

lazy val scala212projects = Seq[ProjectReference](
  zioConfigJS,
  zioConfigJVM,
  zioConfigAwsJVM,
  zioConfigNative,
  zioConfigTypesafeJVM,
  zioConfigDerivationJVM,
  zioConfigYamlJVM,
  docs,
  zioConfigEnumeratumJVM,
  zioConfigCatsJVM,
  zioConfigRefinedJVM,
  zioConfigMagnoliaJVM,
  zioConfigZioAwsJVM,
  zioConfigXmlJVM,
  examplesJVM
)

lazy val scala213projects = scala212projects ++ Seq[ProjectReference](zioConfigScalazJVM)

lazy val scala3projects =
  Seq[ProjectReference](
    zioConfigJVM,
    zioConfigAwsJVM,
    zioConfigZioAwsJVM,
    zioConfigCatsJVM,
    zioConfigDerivationJVM,
    zioConfigEnumeratumJVM,
    zioConfigMagnoliaJVM,
    zioConfigRefinedJVM,
    zioConfigScalazJVM,
    zioConfigTypesafeJVM,
    zioConfigYamlJVM,
    zioConfigXmlJVM,
    docs
  )

lazy val root =
  project
    .in(file("."))
    .settings(publish / skip := true)
    .aggregate(scala213projects: _*)

lazy val `root2-12` =
  project
    .in(file("2-12"))
    .settings(publish / skip := true)
    .aggregate(scala212projects: _*)

lazy val `root2-13` =
  project
    .in(file("2-13"))
    .settings(publish / skip := true)
    .aggregate(scala213projects: _*)

lazy val `root3` =
  project
    .in(file("3"))
    .settings(publish / skip := true)
    .aggregate(scala3projects: _*)

lazy val zioConfig = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("core"))
  .settings(stdSettings("zio-config"))
  .settings(crossProjectSettings)
  .enablePlugins(BuildInfoPlugin)
  .settings(buildInfoSettings("zio.config"))
  .settings(macroDefinitionSettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    libraryDependencies ++= Seq(
      "dev.zio"                %% "zio"                     % zioVersion,
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.8.1",
      "dev.zio"                %% "zio-test"                % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )

lazy val zioConfigJS     = zioConfig.js
  .settings(libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test)
lazy val zioConfigJVM    = zioConfig.jvm
  .settings(dottySettings)
  .settings(libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test)
lazy val zioConfigNative = zioConfig.native
  .settings(nativeSettings)

lazy val zioConfigAws    = crossProject(JVMPlatform)
  .in(file("aws"))
  .settings(stdSettings("zio-config-aws"))
  .settings(crossProjectSettings)
  .settings(dottySettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-ssm" % awsVersion,
      "dev.zio"      %% "zio-streams"      % zioVersion,
      "dev.zio"      %% "zio-test"         % zioVersion % Test,
      "dev.zio"      %% "zio-test-sbt"     % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test")

lazy val zioConfigAwsJVM = zioConfigAws.jvm
  .settings(dottySettings)

lazy val zioConfigZioAws    = crossProject(JVMPlatform)
  .in(file("zio-aws"))
  .settings(stdSettings("zio-config-zio-aws"))
  .settings(crossProjectSettings)
  .settings(dottySettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-aws-ssm"  % zioAwsVersion,
      "dev.zio" %% "zio-streams"  % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test")

lazy val zioConfigZioAwsJVM = zioConfigZioAws.jvm
  .settings(dottySettings)

lazy val zioConfigRefined    = crossProject(JVMPlatform)
  .in(file("refined"))
  .settings(stdSettings("zio-config-refined"))
  .settings(crossProjectSettings)
  .settings(dottySettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    refinedDependencies,
    libraryDependencies ++=
      Seq(
        "dev.zio" %% "zio-test"     % zioVersion % Test,
        "dev.zio" %% "zio-test-sbt" % zioVersion % Test
      ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfigMagnolia % "compile->compile;test->test")

lazy val zioConfigRefinedJVM = zioConfigRefined.jvm

lazy val zioConfigPureconfig    = crossProject(JVMPlatform)
  .in(file("pureconfig"))
  .settings(stdSettings("zio-config-pureconfig"))
  .settings(crossProjectSettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    pureconfigDependencies,
    libraryDependencies ++=
      Seq(
        "dev.zio" %% "zio-test"     % zioVersion % Test,
        "dev.zio" %% "zio-test-sbt" % zioVersion % Test
      ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "test->test", zioConfigTypesafe)

lazy val zioConfigPureconfigJVM = zioConfigPureconfig.jvm

lazy val runAllExamples = taskKey[Unit]("Run all main classes in examples module")

lazy val examples = crossProject(JVMPlatform)
  .in(file("examples"))
  .settings(stdSettings("zio-config-examples"))
  .settings(crossProjectSettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    publish / skip := true,
    fork := true,
    magnoliaDependencies,
    refinedDependencies,
    runAllExamples :=
      Def
        .taskDyn({
          val classes = (Compile / discoveredMainClasses).value
          val runs    = (Compile / runMain)

          val runTasks = classes.map { cc =>
            Def.task {
              runs.toTask(s" ${cc}").value
            }
          }

          Def.sequential(runTasks)
        })
        .value
  )
  .dependsOn(zioConfig, zioConfigMagnolia, zioConfigRefined, zioConfigTypesafe, zioConfigYaml)

lazy val examplesJVM = examples.jvm

lazy val zioConfigDerivation = crossProject(JVMPlatform)
  .in(file("derivation"))
  .settings(stdSettings("zio-config-derivation"))
  .settings(crossProjectSettings)
  .settings(dottySettings)
  .dependsOn(zioConfig)

lazy val zioConfigDerivationJVM = zioConfigDerivation.jvm
  .settings(dottySettings)

lazy val zioConfigMagnolia    = crossProject(JVMPlatform)
  .in(file("magnolia"))
  .settings(stdSettings("zio-config-magnolia"))
  .settings(crossProjectSettings)
  .settings(dottySettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    magnoliaDependencies,
    scalacOptions ++= {
      if (scalaVersion.value == ScalaDotty) {
        Seq.empty
      } else {
        Seq("-language:experimental.macros")
      }
    },
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test", zioConfigDerivation)

lazy val zioConfigMagnoliaJVM = zioConfigMagnolia.jvm

lazy val zioConfigTypesafe    = crossProject(JVMPlatform)
  .in(file("typesafe"))
  .settings(stdSettings("zio-config-typesafe"))
  .settings(crossProjectSettings)
  .settings(dottySettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    libraryDependencies ++= Seq(
      "com.typesafe" % "config"       % "1.4.2",
      "dev.zio"     %% "zio-test"     % zioVersion % Test,
      "dev.zio"     %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test")

lazy val zioConfigTypesafeJVM = zioConfigTypesafe.jvm
  .settings(dottySettings)

lazy val zioConfigYaml    = crossProject(JVMPlatform)
  .in(file("yaml"))
  .settings(stdSettings("zio-config-yaml"))
  .settings(crossProjectSettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    libraryDependencies ++= Seq(
      "org.snakeyaml" % "snakeyaml-engine" % "2.6",
      "dev.zio"      %% "zio-test"         % zioVersion % Test,
      "dev.zio"      %% "zio-test-sbt"     % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test")

lazy val zioConfigYamlJVM = zioConfigYaml.jvm
  .settings(dottySettings)

lazy val zioConfigXml    = crossProject(JVMPlatform)
  .in(file("xml"))
  .settings(stdSettings("zio-config-xml"))
  .settings(crossProjectSettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-parser"   % "0.1.8",
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test")

lazy val zioConfigXmlJVM = zioConfigXml.jvm
  .settings(dottySettings)

lazy val zioConfigScalaz    = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("scalaz"))
  .settings(stdSettings("zio-config-scalaz"))
  .settings(crossProjectSettings)
  .settings(
    crossScalaVersions --= Seq("2.11", Scala212),
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core"  % "7.4.0-M13",
      "dev.zio"    %% "zio-test"     % zioVersion % Test,
      "dev.zio"    %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test")

lazy val zioConfigScalazJVM = zioConfigScalaz.jvm
  .settings(dottySettings)

lazy val zioConfigCats    = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("cats"))
  .settings(stdSettings("zio-config-cats"))
  .settings(crossProjectSettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"    % "2.8.0",
      "dev.zio"       %% "zio-test"     % zioVersion % Test,
      "dev.zio"       %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test")

lazy val zioConfigCatsJVM = zioConfigCats.jvm
  .settings(dottySettings)

lazy val zioConfigEnumeratum    = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("enumeratum"))
  .settings(stdSettings("zio-config-enumeratum"))
  .settings(crossProjectSettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    libraryDependencies ++= Seq(
      "com.beachape" %% "enumeratum"   % "1.7.0",
      "dev.zio"      %% "zio-test"     % zioVersion % Test,
      "dev.zio"      %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test")

lazy val zioConfigEnumeratumJVM = zioConfigEnumeratum.jvm

lazy val zioConfigTypesafeMagnoliaTests    = crossProject(JVMPlatform)
  .in(file("typesafe-magnolia-tests"))
  .settings(stdSettings("zio-config-typesafe-magnolia-tests"))
  .settings(crossProjectSettings)
  .settings(
    crossScalaVersions --= Seq("2.11"),
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.typesafe" % "config"       % "1.4.2",
      "dev.zio"     %% "zio-test"     % zioVersion % Test,
      "dev.zio"     %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .dependsOn(zioConfig % "compile->compile;test->test", zioConfigTypesafe, zioConfigMagnolia)
lazy val zioConfigTypesafeMagnoliaTestsJVM = zioConfigTypesafeMagnoliaTests.jvm

lazy val docs = project
  .in(file("zio-config-docs"))
  .settings(
    moduleName := "zio-config-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    magnoliaDependencies,
    refinedDependencies,
    crossScalaVersions := (zioConfigJVM / crossScalaVersions).value,
    projectName := "ZIO Config",
    mainModuleName := (zioConfigJVM / moduleName).value,
    projectStage := ProjectStage.ProductionReady,
    ScalaUnidoc / unidoc / unidocProjectFilter :=
      inProjects(
        zioConfigJVM,
        zioConfigTypesafeJVM,
        zioConfigDerivationJVM,
        zioConfigYamlJVM,
        zioConfigRefinedJVM,
        zioConfigMagnoliaJVM
      )
  )
  .settings(macroDefinitionSettings)
  .dependsOn(
    zioConfigJVM,
    zioConfigTypesafeJVM,
    zioConfigDerivationJVM,
    zioConfigYamlJVM,
    zioConfigRefinedJVM,
    zioConfigMagnoliaJVM
  )
  .enablePlugins(WebsitePlugin)
