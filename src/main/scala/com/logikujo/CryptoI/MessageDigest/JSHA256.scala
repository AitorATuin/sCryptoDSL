package com.logikujo.CryptoI.MessageDigest

import com.logikujo.CryptoI._
import java.security.{MessageDigest => MD}

object JSHA256 {
  sealed trait JSHA256 extends MessageDigestSpec

  implicit object JSHA256MessageDigest
      extends MessageDigest[JSHA256]
  {
    def digest(msg: Hex): Hex =
      Hex(MD.getInstance("SHA-256").digest(msg.value))
  }
}
