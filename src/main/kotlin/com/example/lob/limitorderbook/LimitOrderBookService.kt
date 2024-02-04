package com.example.lob.limitorderbook

import com.example.lob.currencypair.CurrencyPair
import com.example.lob.order.BuyOrSellEnum
import com.example.lob.order.OrderDto
import com.example.lob.order.OrderResponseDto
import com.example.lob.security.AuthenticationFacade
import com.example.lob.trade.Trade
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class LimitOrderBookService(
    private val limitOrderBookFactory: LimitOrderBookFactory,
    private val authenticationFacade: AuthenticationFacade
) {

    fun addOrder(orderDto: OrderDto) {
        val order = orderDto.toOrder(authenticationFacade.getAuthentication().name)
        val orderBook = limitOrderBookFactory.getOrderBook(orderDto.currencyPair)
        orderBook.addOrder(order)
    }

    fun getOrderBook(currencyPair: CurrencyPair): OrderBookDto {
        val orderBook = limitOrderBookFactory.getOrderBook(currencyPair)
        return OrderBookDto(
            asks = orderBook.getBestAsks(40) { limitPriceOrder ->
                OrderResponseDto(
                    side = BuyOrSellEnum.SELL,
                    quantity = limitPriceOrder.quantity,
                    price = limitPriceOrder.price,
                    currencyPair = currencyPair,
                    orderCount = limitPriceOrder.orderCount
                )
            },
            bids = orderBook.getBestBids(40) { limitPriceOrder ->
                OrderResponseDto(
                    side = BuyOrSellEnum.BUY,
                    quantity = limitPriceOrder.quantity,
                    price = limitPriceOrder.price,
                    currencyPair = currencyPair,
                    orderCount = limitPriceOrder.orderCount
                )
            },
        )
    }

    fun getTradeHistory(
        pair: CurrencyPair,
        startTime: ZonedDateTime,
        endDateTime: ZonedDateTime,
        skip: Int,
        limit: Int
    ): List<Trade> {
        val orderBook = limitOrderBookFactory.getOrderBook(pair)
        return orderBook.getTradeHistory(startTime, endDateTime, skip, limit)
    }

}
