package com.example.lob.order

import com.example.lob.currencypair.CurrencyPair

data class OrderResponseDto(
    val side: BuyOrSellEnum,
    val quantity: Double,
    val price: Double,
    val currencyPair: CurrencyPair,
    val orderCount: Int
)
