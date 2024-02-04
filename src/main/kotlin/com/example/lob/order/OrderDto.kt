package com.example.lob.order

import com.example.lob.currencypair.CurrencyPair

class OrderDto(
    val price: Double,
    val quantity: Double,
    val currencyPair: CurrencyPair,
    val buyOrSellEnum: BuyOrSellEnum
) {
    fun toOrder(username: String): Order {
        return Order(
            price = price,
            quantity = quantity,
            currencyPair = currencyPair,
            username = username,
            buyOrSellEnum = buyOrSellEnum,
            orderId = OrderId(),
        )
    }
}
