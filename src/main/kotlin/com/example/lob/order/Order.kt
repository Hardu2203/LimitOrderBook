package com.example.lob.order

import com.example.lob.currencypair.CurrencyPair
import java.math.BigDecimal

data class Order(
    val price: BigDecimal,
    var quantity: BigDecimal,
    val currencyPair: CurrencyPair,
    var volume: BigDecimal = price * quantity,
    var username: String,
    val buyOrSellEnum: BuyOrSellEnum,
    val orderId: OrderId = OrderId()
)
