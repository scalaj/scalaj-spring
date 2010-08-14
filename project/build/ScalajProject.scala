import sbt._
 
class ScalajProject(info: ProjectInfo) extends DefaultProject(info) with posterous.Publish {
  override def compileOptions = Seq(Deprecation, Unchecked)

  // Dependencies
  val scalap = "org.scala-lang" % "scalap" % "2.8.0" withSources()
  val scalatest = "org.scalatest" % "scalatest" % "1.2-for-scala-2.8.0.final-SNAPSHOT" % "test"
  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"
  val spring_core = "org.springframework" % "spring-core" % "3.0.3.RELEASE" withSources()
  val spring_context = "org.springframework" % "spring-context" % "3.0.3.RELEASE" withSources()

  // Repositories
  val scalaToolsSnapshots = "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-snapshots"

  // Publishing
  override def managedStyle = ManagedStyle.Maven
  val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
  Credentials(Path.userHome / ".ivy2" / ".credentials", log)
  override def publishAction = super.publishAction && publishCurrentNotes
  override def extraTags = "scalaj" :: super.extraTags
}
