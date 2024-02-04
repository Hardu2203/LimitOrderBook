package com.example.lob.trade

import com.example.lob.currencypair.CurrencyPair
import com.example.lob.order.BuyOrSellEnum
import java.time.ZonedDateTime
import java.util.*

data class Trade(
    val price: Double,
    val quantity: Double,
    val currencyPair: CurrencyPair,
    val tradedAt: ZonedDateTime,
    val takerSide: BuyOrSellEnum,
    val quoteVolume: Double = price * quantity
) {
    val id: UUID = UUID.randomUUID()
}
