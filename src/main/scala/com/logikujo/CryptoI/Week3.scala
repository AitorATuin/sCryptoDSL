package com.logikujo.CryptoI

import scala.annotation.tailrec
import Implicits._
import MessageDigest.JSHA256._
import scalaz._
import Scalaz._
import java.io.{FileInputStream => FIStream, BufferedInputStream => BFStream, RandomAccessFile}
import java.nio.file.{Paths, Path}

object Week3 {

  abstract class Video
  {
    val name: String

    private def hash(msg: Hex):Hex = PlainMsg(msg).digest.payload
    private def hash(msg1: Hex, msg2: Hex): Hex = PlainMsg(msg1 | msg2).digest.payload

    def canRead: Boolean = Paths.get(name).toFile.canRead

    lazy val positions: Validation[String, Stream[(Int,Int)]] = (canRead)? {
      val randomBuffer = new RandomAccessFile(name, "r")
      val len = randomBuffer.length.toInt
      randomBuffer.close()
      (0 to len by 1024).reverse.zipAll(List(len % 1024), 0, 1024).toStream.success[String]
    } | ("Unable to read file".failure[Stream[(Int,Int)]])

    def computeHash: Validation[String,List[Hex]] = {
      val randomBuffer = new RandomAccessFile(name, "r")
      val readHex = ((randomBuffer:RandomAccessFile) =>
        (pos:Int, size:Int) => {
          val a:Array[Byte] = new Array(size)
          randomBuffer.seek(pos)
          randomBuffer.read(a)
          Hex(a)
        })(randomBuffer)
      val res = positions flatMap (s => s match {
        case x #:: xs => xs.scanLeft(hash(readHex(x._1, x._2)))((b,a) => {
          //if (a.isEmpty) randomBuffer.close()
          hash(readHex(a._1, a._2), b)
        }).toList.reverse.success[String]
      })
      randomBuffer.close()
      res
    }
  }
  // case class Video(file: Path, positions: Stream[(Int,Int)])

  object Video {
    def apply(n:String): Video = new Video { val name = n }
    def get(name:String):Validation[String,Video] = (Paths.get(name).toFile.canRead)?
      (this(name).success[String])|
      ("Unable to read file".failure[Video])
  }

  def main(args:Array[String]):Unit = {
    val fileName = args.lift(0).toSuccess("Pass a file as argument")
    val h0 = (fileName.flatMap(s => Video(s).computeHash).flatMap(_.head.success))
    println(h0)
  }
}
