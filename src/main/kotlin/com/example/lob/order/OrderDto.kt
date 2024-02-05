package com.example.lob.order

import com.example.lob.currencypair.CurrencyPair
import java.math.BigDecimal

class OrderDto(
    val price: BigDecimal,
    val quantity: BigDecimal,
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
