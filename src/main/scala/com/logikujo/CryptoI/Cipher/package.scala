package com.logikujo.CryptoI

/**
 * sCryptoDSL / 2013 / Logikujo.com [Fun Dev]
 *
 * com.logikujo.CryptoI.Cipher / atuin / 23/04/13 / 15:44
 *
 *
 */
package object Cipher {
  trait CipherSpec

  trait CipherEncrypt[CipherSpec] {
    def encrypt(msg: Hex, key: Hex): Hex
  }

  trait CipherDecrypt[CipherSpec] {
    def decrypt(msg: Hex, key: Hex): Hex
  }
}
