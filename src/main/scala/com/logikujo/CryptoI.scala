package com.logikujo.CryptoI

import javax.crypto.Cipher
import javax.crypto.spec.{SecretKeySpec, IvParameterSpec}
import org.apache.commons.codec.binary.{Base64 => B64}
import java.security.SecureRandom
import scala.util.Random

trait CipherSpec

trait CipherEncrypt[CipherSpec] {
  def encrypt(msg: Hex, key: Hex): Hex
}

trait CipherDecrypt[CipherSpec] {
  def decrypt(msg: Hex, key: Hex): Hex
}

trait Base64 {
  val contents: Array[Byte]
  def apply(): Array[Byte] = contents
 // def xor(other:Base64): Base64 = 
}

trait AsBase64[T] {
  def toBase64(v:T): Base64
  def fromBase64(b64:Base64): T
}

case class Hex(value:Array[Byte]) {
  lazy val length: Int = value length
  override def toString = value map ("%02x" format (_))  mkString
  def xor(other: Hex): Hex = Hex((value zip other.value) map {
    case (a,b) => (a ^ b).toByte
  })
}

trait AsHex[T] {
  def encode(v:T): Hex
  def decode(h: Hex): T
}

trait Msg[T] {
  val payload: Hex

  def apply(): Hex = payload
  override def toString: String = payload toString
}

/* Need to be abstact class in order to use context bound AsHex[T] */
abstract class PlainMsg[T: AsHex] extends Msg[T] {
  val value: T

  def encrypt[C](key: Msg[_])(implicit c: CipherEncrypt[C]): CipherMsg[T] =
    CipherMsg(c.encrypt(payload, key.payload))
}

abstract class CipherMsg[T: AsHex] extends Msg[T] {
  def decrypt[C](key: Msg[_])(implicit c: CipherDecrypt[C]): PlainMsg[T] =
    PlainMsg(c.decrypt(payload, key.payload))
}

object PlainMsg {
  def apply[A](v:A)(implicit h:AsHex[A]): PlainMsg[A] = new PlainMsg[A] {
    val value:A = v
    val payload = h encode v
  }
  def apply[A](ha:Hex)(implicit h:AsHex[A]): PlainMsg[A] =
    new PlainMsg[A] {
      val value:A = h decode ha
      val payload = ha
    }
}

object CipherMsg {
  def apply[A](v:Array[Byte])(implicit h:AsHex[A]): CipherMsg[A] =
    new CipherMsg[A] {
      val payload:Hex = Hex(v)
    }
  def apply[A](ha:Hex)(implicit h:AsHex[A]): CipherMsg[A] = 
    new CipherMsg[A] {
      val payload:Hex = ha
    }
}

object Implicits {
  sealed trait StringAsMsg {
    val value: String

    private def fromHex(s:String):Hex =
      Hex(s grouped(2) map {Integer.parseInt(_,16).toByte} toArray)

    def asciiPlain(implicit h:AsHex[String]): PlainMsg[String] = PlainMsg(value)
    def hexPlain(implicit h:AsHex[String]): PlainMsg[String] = 
      PlainMsg(fromHex(value))
    def hex(implicit h:AsHex[String]): CipherMsg[String] = 
      CipherMsg(fromHex(value))
  }

  implicit object StringAsBase64 extends AsBase64[String] {
    def toBase64(s: String):Base64 = new Base64 {
      val contents: Array[Byte] = B64.encodeBase64(s.toCharArray.map(_.toByte))
    }
    def fromBase64(b64: Base64):String = B64.decodeBase64(b64()) map { b =>
      (b & 0xff).toChar
    } mkString
  }

  implicit def stringAsMsg(text:String) = new StringAsMsg {
    val value = text
  }

  implicit object StringAsHex extends AsHex[String] {
    def encode(s:String):Hex = Hex(s.toCharArray.map(_.toByte))
    def decode(h:Hex):String = h.value map (b => (b & 0xff).toChar) mkString
  }
}
/*  sealed trait OTP extends CipherSpec

  implicit object OTPCipher 
      extends CipherDecrypt[OTP] 
      with CipherEncrypt[OTP] {
    def encrypt(msg: Hex, key: Hex) = msg xor key
    def decrypt(msg: Hex, key: Hex) = msg xor key
  }*/

/*  implicit object AESCipher extends CipherEncrypt[AES] with CipherDecrypt[AES] {
//    lazy val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
    lazy private val E = cipherWithKey(Cipher.ENCRYPT_MODE)
    lazy private val D = cipherWithKey(Cipher.DECRYPT_MODE)
    lazy private val PADDING_SIZE = 16

    private def padding(msg:Hex):Hex = {
      val p = PADDING_SIZE - msg.length % PADDING_SIZE
      Hex(msg.value ++ (1 to p).map (_.toByte))
    }
    private def cipherWithKey(mode:Int) = 
      (key: Hex) => (msg:Hex) => {
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        mode match {
          case Cipher.ENCRYPT_MODE => {
            val IV = new Array[Byte](16)
            new Random(new SecureRandom).nextBytes(IV)
            c.init(mode, 
              new SecretKeySpec(key.value, "AES"),
              new IvParameterSpec(IV))
          }
          case Cipher.DECRYPT_MODE => {
            val IV = c.getIV()
            c.init(mode, 
              new SecretKeySpec(key.value, "AES"),
              new IvParameterSpec(IV))
          }
        }
        c.doFinal(msg.value)
      }

    def encrypt(msg: Hex, key: Hex): Hex = Hex(E(key)(msg))
    def decrypt(msg: Hex, key: Hex): Hex = Hex(D(key)(msg))
  }*/

