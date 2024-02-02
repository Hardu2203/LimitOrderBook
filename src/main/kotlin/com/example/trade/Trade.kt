package com.example.trade

import com.example.order.BuyOrSellEnum
import java.time.ZonedDateTime
import java.util.*

data class Trade(
    val price: Double,
    val quantity: Double,
    val currencyPair: String,
    val tradedAt: ZonedDateTime,
    val takerSide: BuyOrSellEnum,
    val quoteVolume: Double = price * quantity
) {
    val id: UUID = UUID.randomUUID()
}
