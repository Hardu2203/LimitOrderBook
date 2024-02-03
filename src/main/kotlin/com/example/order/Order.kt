package com.example.order

import com.example.trade.CurrencyPair

data class Order(
    val price: Double,
    val quantity: Double,
    val currencyPair: CurrencyPair,
    var volume: Double = price * quantity,
    var username: String,
    val buyOrSellEnum: BuyOrSellEnum,
    val orderId: OrderId = OrderId()
)
