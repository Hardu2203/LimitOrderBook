package com.example.lob.order

import com.example.lob.currencypair.CurrencyPair
import java.math.BigDecimal

data class OrderResponseDto(
    val side: BuyOrSellEnum,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val currencyPair: CurrencyPair,
    val orderCount: Int
)
