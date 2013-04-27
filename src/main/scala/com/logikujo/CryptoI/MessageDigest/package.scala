package com.logikujo.CryptoI

package object MessageDigest {
  trait MessageDigestSpec

  trait MessageDigest[MessageDigestSpec] {
    def digest(msg: Hex): Hex
  }
}
