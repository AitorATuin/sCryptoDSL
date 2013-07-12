package com.logikujo.CryptoI

/**
 *
 * sCryptoDSL / LogiDev - [Fun Functional] / Logikujo.com
 *
 * com.logikujo.CryptoI 4/05/13 :: 17:44 :: eof
 *
 */

import Implicits._
import dispatch._
import Defaults._
import com.ning.http.client.RequestBuilder

object Week4 {
  val targetHost:RequestBuilder = host("crypto-class.appspot.com") / "po"
  val queryParam = "er"
  val goodQueryValue = "f20bdba6ff29eed7b046d1df9fb7000058b1ffb4210a580f748b4ac714c001bd4a61044426fb515dad3f21f18aa577c0bdf302936266926ff37dbf7035d5eeb4"

  trait AttackStatus
  case class Success() extends AttackStatus
  case object BadPadding extends AttackStatus
  case class BadMessage(pad: Int, msg: Hex) extends AttackStatus
  case object Unknow extends AttackStatus

  trait AttackRequest {
    val createRequest: () => RequestBuilder
    val ct: Hex
    val iv: Hex
    lazy val blocksStream: Stream[Hex] = ct.grouped(16).toStream

    private def nullIV(n: Int): Hex = List.fill(n)("00").mkString.hex
    private def prepareFakeIV(pad: Int): Hex = nullIV((16 - pad) + 1) | Hex(pad - 1)(pad.toByte)
    private def prepareFakeIV(interValue: Hex, pad: Int): Hex =
      interValue xor prepareFakeIV(pad)

    def guessResponse(h: Hex) = Http(createRequest() <<? Map("er" -> h.toString) OK as.String).either

    def guess(block: Hex)(iv: Hex)(pad: Int):AttackStatus = {
      val m = iv | block
      val response = Http(createRequest() <<? Map("er" -> m.toString) OK as.String).either
      response() match {
        case Right(_) => Success()
        case Left(StatusCode(404)) => BadMessage(pad, (iv xor (nullIV(16 - pad) | Hex(pad)(pad.toByte))))
        case Left(StatusCode(403)) => BadPadding
        case Left(StatusCode(_)) => Unknow
      }
    }

    def breakByte(n: Int)(pad: Int)(interValue: Hex): Option[AttackStatus] = {
      print("+-------> Trying padding 0x%02x: ".format(pad))
      val block = blocksStream.drop(n - 1).take(16).head
      val f = guess(block)_
      val iv = interValue xor prepareFakeIV(pad)
      Iterator.tabulate(0xff) {
        i => {
          if (i % 2 == 0)
            print(".")
          val ivFake = iv.setByte(16 - pad, i.toByte)
          f(ivFake)(pad)
        }
      }.find {
          case BadMessage(_,inter) => {
            if (inter(pad).isDefined)
              println(" SUCCESS: %02x".format(inter(16 - pad).getOrElse(0)))
            else
              println(" SUCCESS: 0x??")
            true
          }
          case _ => false
      }
    }

    private def recBreakBlock(f: Int => Hex => Option[AttackStatus],
                            pad: Int,
                            interValue:Option[AttackStatus]): Option[Hex] =
      if (pad > 16) interValue match {
        case Some(BadMessage(_, inter)) => Some(inter)
        case _ => None
      }
      else {
        interValue match {
          case Some(BadMessage(_, inter)) => recBreakBlock(f, pad + 1, f(pad)(inter))
          case _ => None
        }
      }

    def breakBlock(n: Int): Option[Hex] = {
      println("+ Breaking block: %d".format(n))
      val f = breakByte(n)_
      recBreakBlock(f, 1, Some(BadMessage(1,prepareFakeIV(1))))
    }

    def breakBlock(n:Int, interValue: Hex) = {
      val pad = (16 - interValue.value.dropWhile(0 ==).length) + 1
      val f = breakByte(n)_
      recBreakBlock(f, pad, Some(BadMessage(pad, interValue)))
    }
  }

  trait Attack extends AttackRequest
  object Attack {
    def apply(f: () => RequestBuilder) = (h: Hex) => new AttackRequest {
      val createRequest = f
      val ct = h.drop(16)
      val iv = h.take(16)
    }
  }

  val cryptoIAttack = Attack(() => host("crypto-class.appspot.com") / "po")

  /*
  res2: String = "The Magic Words "
  res5: String = are Squeamish Os
  "sifrage									"
*/
}
