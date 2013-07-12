name := "CrytpoI Course Homework"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

resolvers += "iBlibio" at "http://http://mirrors.ibiblio.org/maven2/"

resolvers += "spray repo" at "http://repo.spray.io"
 
libraryDependencies += "commons-codec" % "commons-codec" % "1.7"

libraryDependencies += "org.scalaz" % "scalaz-core_2.10" % "7.0.0-M7"

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.10.0"

libraryDependencies +=  "io.spray" % "spray-client" % "1.1-M7"

//initialCommands in console := "import scalaz._, Scalaz._"

//initialCommands in console := "import com.logikujo.CryptoI"
