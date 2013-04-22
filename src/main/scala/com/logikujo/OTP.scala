package com.logikujo.CryptoI

package Cipher {
  package object OTP {
    sealed trait OTP extends CipherSpec

    implicit object OTPCipher
      extends CipherDecrypt[OTP]
        with CipherEncrypt[OTP] {
      def encrypt(msg: Hex, key: Hex) = msg xor key
      def decrypt(msg: Hex, key: Hex) = msg xor key
    }
  }
}
