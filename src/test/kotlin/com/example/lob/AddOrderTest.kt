package com.example.lob

import com.example.lob.order.BuyOrSellEnum
import com.example.lob.order.Order
import com.example.lob.trade.CurrencyPair
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AddOrderTest {

    private lateinit var limitOrderBook: LimitOrderBook

    @BeforeEach
    fun setUp() {
        limitOrderBook = LimitOrderBook()
    }

    @Nested
    @DisplayName("Bid/Buy")
    inner class BidBuy {

        @Test
        @DisplayName("Should add bid/buy order to order book")
        fun shouldAddOrdersToBook() {
            //given
            val buyOrder1 = Order(
                price = 10.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val buyOrder2 = Order(
                price = 20.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )

            //when
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(buyOrder2)

            //then
            Assertions.assertEquals(20.0, limitOrderBook.getBestBidOrNull()?.price)
            Assertions.assertEquals(buyOrder1, limitOrderBook.getOrderQueue(10.0, BuyOrSellEnum.BUY)?.orders?.peek())
            Assertions.assertEquals(buyOrder2, limitOrderBook.getOrderByIdOrNull(buyOrder2.orderId))
            Assertions.assertEquals(100.0, limitOrderBook.getVolume(20.0, BuyOrSellEnum.BUY))
        }

        @Test
        @DisplayName("Order volume should match bid/buy orders at price")
        fun orderVolumeShouldMatchBidBuyOrdersAtPrice() {
            val buyOrder1 = Order(
                price = 10.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val buyOrder2 = Order(
                price = 10.0,
                quantity = 20.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )

            //when
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(buyOrder2)

            //then
            Assertions.assertEquals(2, limitOrderBook.getOrderQueue(10.0, BuyOrSellEnum.BUY)?.orders?.size)
            Assertions.assertEquals(250.0, limitOrderBook.getVolume(10.0, BuyOrSellEnum.BUY))
        }

    }

    @Nested
    @DisplayName("Ask/Sell")
    inner class AskSell {

        @Test
        @DisplayName("Should add ask/sell order to order book")
        fun shouldAddOrdersToBook() {
            //given
            val sellOrder1 = Order(
                price = 80.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val sellOrder2 = Order(
                price = 50.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )

            //when
            limitOrderBook.addOrder(sellOrder1)
            limitOrderBook.addOrder(sellOrder2)

            //then
            Assertions.assertEquals(50.0, limitOrderBook.getBestAskOrNull()?.price)
            Assertions.assertEquals(sellOrder1, limitOrderBook.getOrderQueue(80.0, BuyOrSellEnum.SELL)?.orders?.peek())
            Assertions.assertEquals(sellOrder2, limitOrderBook.getOrderByIdOrNull(sellOrder2.orderId))
            Assertions.assertEquals(400.0, limitOrderBook.getVolume(80.0, BuyOrSellEnum.SELL))
        }

        @Test
        @DisplayName("Order volume should match ask/sell orders at price")
        fun orderVolumeShouldMatchAskSellOrdersAtPrice() {
            //given
            val sellOrder1 = Order(
                price = 50.0,
                quantity = 10.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val sellOrder2 = Order(
                price = 50.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )

            //when
            limitOrderBook.addOrder(sellOrder1)
            limitOrderBook.addOrder(sellOrder2)

            //then
            Assertions.assertEquals(2, limitOrderBook.getOrderQueue(50.0, BuyOrSellEnum.SELL)?.orders?.size)
            Assertions.assertEquals(750.0, limitOrderBook.getVolume(50.0, BuyOrSellEnum.SELL))
        }

    }



}
