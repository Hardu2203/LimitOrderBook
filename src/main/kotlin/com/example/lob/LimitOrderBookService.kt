package com.example.lob

import com.example.order.BuyOrSellEnum
import com.example.order.Order
import com.example.order.OrderId
import org.springframework.stereotype.Service
import kotlin.math.min
import mu.KotlinLogging
import java.util.*

@Service
class LimitOrderBookService(
    private val limitOrderBookFactory: LimitOrderBookFactory
) {

    fun addOrder(order: Order) {
        val limitOrderBook = limitOrderBookFactory.getOrderBook(order.currencyPair)
        limitOrderBook.addOrder(order)
    }

}

private val logger = KotlinLogging.logger {}
