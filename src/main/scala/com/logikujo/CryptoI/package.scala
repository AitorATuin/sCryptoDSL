package com.logikujo

/**
 * sCryptoDSL / 2013 / Logikujo.com [Fun Dev]
 *
 * com.logikujo.CryptoI / atuin / 23/04/13 / 15:41
 *
 *
 */
import org.apache.commons.codec.binary.{Base64 => B64}

package object CryptoI {
  import Cipher._
  import Mac._
  import MessageDigest._

  trait Base64 {
    val contents: Array[Byte]

    def apply(): Array[Byte] = contents

    // def xor(other:Base64): Base64 =
  }

  trait AsBase64[T] {
    def toBase64(v: T): Base64

    def fromBase64(b64: Base64): T
  }

  case class Hex(value: Array[Byte]) {
    lazy val length: Int = value.length

    override def toString = value.map("%02x" format (_)).mkString

    def | (other: Hex): Hex = Hex(value ++ other.value)

    def xor(other: Hex): Hex = Hex((value zip other.value) map {
      case (a, b) => (a ^ b).toByte
    })

    def take(n: Int): Hex = Hex(value.take(n))
    def drop(n: Int): Hex = Hex(value.drop(n))
    def setByte(byte: Int, f: Byte => Byte): Hex = value.lift(byte) match {
      case Some(b) =>
        Hex(value.take(byte) ++ Array(f(b)) ++ value.drop(byte+1))
      case None => Hex(value)
    }
    def setByte(byte: Int, b: Byte):Hex = setByte(byte, (_:Byte) => b)

    def grouped(n: Int): Iterator[Hex] = value.grouped(n).map(Hex(_))

    def apply(n: Int) = value.lift(n)
  }

  object Hex {
    def parseHexString(s: String): Hex =
        Hex(s grouped (2) map {
          Integer.parseInt(_, 16).toByte
        } toArray)

    def apply(n:Int)(v:Byte): Hex = Hex(Array.fill(n)(v))
  }

  trait ToHex[T] {
    def encode(v: T): Hex

    def decode(h: Hex): T
  }

  trait Msg[T] {
    val payload: Hex

    def apply(): Hex = payload

    override def toString: String = payload.toString
  }

  /* Need to be abstact class in order to use context bound ToHex[T] */
  abstract class PlainMsg[T: ToHex] extends Msg[T] {
    val value: T

    def encrypt[C](key: Msg[_])(implicit c: CipherEncrypt[C]): CipherMsg[T] =
      CipherMsg(c.encrypt(payload, key.payload))

    def mac[M](key: Msg[_])(implicit m: Mac[M]): CipherMsg[T] = CipherMsg(m.mac(payload, key.payload))

    def digest[D](implicit d: MessageDigest[D]): CipherMsg[T] = CipherMsg(d.digest(payload))
  }

  abstract class CipherMsg[T: ToHex] extends Msg[T] {
    def decrypt[C](key: Msg[_])(implicit c: CipherDecrypt[C]): PlainMsg[T] =
      PlainMsg(c.decrypt(payload, key.payload))
  }

  object PlainMsg {
    def apply[A](v: A)(implicit h: ToHex[A]): PlainMsg[A] = new PlainMsg[A] {
      val value: A = v
      val payload = h encode v
    }

    def apply[A](ha: Hex)(implicit h: ToHex[A]): PlainMsg[A] =
      new PlainMsg[A] {
        val value: A = h decode ha
        val payload = ha
      }
  }

  object CipherMsg {
    def apply[A](v: Array[Byte])(implicit h: ToHex[A]): CipherMsg[A] =
      new CipherMsg[A] {
        val payload: Hex = Hex(v)
      }

    def apply[A](ha: Hex)(implicit h: ToHex[A]): CipherMsg[A] =
      new CipherMsg[A] {
        val payload: Hex = ha
      }
  }

  object Implicits {

    sealed trait StringAsMsg {
      val value: String

     /* private def fromHex(s: String): Hex =
        Hex(s grouped (2) map {
          Integer.parseInt(_, 16).toByte
        } toArray)*/

      def asciiPlain(implicit h: ToHex[String]): PlainMsg[String] = PlainMsg(value)

      def hexPlain(implicit h: ToHex[String]): PlainMsg[String] =
        PlainMsg(Hex.parseHexString(value))

      def hex(implicit h: ToHex[String]): Hex = Hex.parseHexString(value)
    }

    implicit object StringAsBase64 extends AsBase64[String] {
      def toBase64(s: String): Base64 = new Base64 {
        val contents: Array[Byte] = B64.encodeBase64(s.toCharArray.map(_.toByte))
      }

      def fromBase64(b64: Base64): String = B64.decodeBase64(b64()).map {
        b =>
          (b & 0xff).toChar
      }.mkString
    }

    implicit def stringAsMsg(text: String) = new StringAsMsg {
      val value = text
    }

    implicit object StringToHex extends ToHex[String] {
      def encode(s: String): Hex = Hex(s.toCharArray.map(_.toByte))

      def decode(h: Hex): String = h.value.map(b => (b & 0xff).toChar).mkString
    }
  }
}
