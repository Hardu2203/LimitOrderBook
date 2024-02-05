package com.example.lob.trade

import com.example.lob.currencypair.CurrencyPair
import com.example.lob.order.BuyOrSellEnum
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*

data class Trade(
    val price: BigDecimal,
    val quantity: BigDecimal,
    val currencyPair: CurrencyPair,
    val tradedAt: ZonedDateTime,
    val takerSide: BuyOrSellEnum,
    val quoteVolume: BigDecimal = price * quantity
) {
    val id: UUID = UUID.randomUUID()
}
