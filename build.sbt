name := "CrytpoI Course Homework"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "iBlibio" at "http://http://mirrors.ibiblio.org/maven2/"
 
//libraryDependencies += "commons-codec" % "commons-codec" % "1.6" 

libraryDependencies ++= Seq(
  "org.scalaz" % "scalaz-core_2.10" % "7.0.0-M7", //cross CrossVersion.full
//  "org.scalaz" % "scalaz-iteratee_2.10" % "7.0.0-M7",
//  "org.scalaz" % "scalaz-effect_2.10" % "7.0.0-M7",
  "commons-codec" % "commons-codec" % "1.6"
)
 
//initialCommands in console := "import scalaz._, Scalaz._"

initialCommands in console := "import com.logikujo.CryptoI"
