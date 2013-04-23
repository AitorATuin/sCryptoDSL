package com.logikujo.CryptoI

/**
 * sCryptoDSL / 2013 / Logikujo.com [Fun Dev]
 *
 * com.logikujo.CryptoI.Mac / atuin / 23/04/13 / 15:45
 *
 *
 */
package object Mac {
  trait MacSpec

  trait Mac[MacSpec] {
    def mac(msg: Hex, key: Hex): Hex
  }
}
