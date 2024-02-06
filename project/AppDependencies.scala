import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.4.0"
  private val hmrcMongoVersion = "1.7.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion % Test
  )

  val it = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion % Test,
    "org.wiremock" % "wiremock" % "3.3.1" % Test excludeAll (
      ExclusionRule("org.slf4j"),
      ExclusionRule("com.fasterxml.jackson.core"),
      ExclusionRule("com.fasterxml.jackson.datatype")
    )
  )
}
