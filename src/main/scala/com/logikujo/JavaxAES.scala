package com.logikujo.CryptoI

import javax.crypto.{Cipher => C}
import javax.crypto.spec.{SecretKeySpec, IvParameterSpec}
import java.security.SecureRandom
import scala.util.Random

package Cipher {

  package object JavaxAES {
    sealed trait JavaxAES extends CipherSpec

    implicit object AESCipher
        extends CipherEncrypt[JavaxAES]
        with CipherDecrypt[JavaxAES] {

      lazy private val E = cipherWithKey(C.ENCRYPT_MODE)
      lazy private val D = cipherWithKey(C.DECRYPT_MODE)
      lazy private val PADDING_SIZE = 16

      private def padding(msg:Hex):Hex = {
        val p = PADDING_SIZE - msg.length % PADDING_SIZE
        Hex(msg.value ++ (1 to p).map (_.toByte))
      }
      private def cipherWithKey(mode:Int) =
        (key: Hex) => (msg:Hex) => {
          val c = C.getInstance("AES/CBC/PKCS5Padding")
          mode match {
            case C.ENCRYPT_MODE => {
              val IV = new Array[Byte](16)
              new Random(new SecureRandom).nextBytes(IV)
              c.init(mode,
                new SecretKeySpec(key.value, "AES"),
                new IvParameterSpec(IV))
            }
            case C.DECRYPT_MODE => {
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
    }
  }
}
