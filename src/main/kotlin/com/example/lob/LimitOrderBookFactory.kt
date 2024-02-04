package com.example.lob

import com.example.lob.trade.CurrencyPair
import org.springframework.stereotype.Component

@Component
class LimitOrderBookFactory {

    private val orderBookMap: MutableMap<CurrencyPair, LimitOrderBook> = mutableMapOf()

    fun getOrderBook(currencyPair: CurrencyPair): LimitOrderBook {
        return orderBookMap.getOrPut(currencyPair) { LimitOrderBook() }
    }

}
