package com.logikujo.CryptoI.Mac

/**
 * ${PROJECT_NAME} / 2013 / Logikujo.com [Fun Dev]
 *
 * ${PACKAGE_NAME} / atuin / 23/04/13 / 00:55
 *
 */

import com.logikujo.CryptoI._
import javax.crypto.{Mac => M}
import javax.crypto.spec.SecretKeySpec

object JavaxHmacSHA256 {
    sealed trait JavaxHmacSHA256 extends MacSpec

    implicit object HmacSHA256 extends Mac[JavaxHmacSHA256]
    {
      def mac(msg:Hex, key: Hex): Hex = {
        val m = M.getInstance("HmacSHA256")
        m.init(new SecretKeySpec(key.value, "HmacSHA256"))
        Hex(m.doFinal(msg.value))
      }
    }
  }
