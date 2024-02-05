package com.example.lob

import com.example.lob.currencypair.CurrencyPair
import com.example.lob.limitorderbook.LimitOrderBook
import com.example.lob.order.BuyOrSellEnum
import com.example.lob.order.Order
import org.junit.jupiter.api.*
import java.math.BigDecimal

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
                price = BigDecimal(10),
                quantity = BigDecimal(5),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val buyOrder2 = Order(
                price = BigDecimal(20),
                quantity = BigDecimal(5),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )

            //when
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(buyOrder2)

            //then
            Assertions.assertEquals(BigDecimal(20), limitOrderBook.getBestBidOrNull()?.price)
            Assertions.assertEquals(buyOrder1, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.BUY)?.orders?.peek())
            Assertions.assertEquals(buyOrder2, limitOrderBook.getOrderByIdOrNull(buyOrder2.orderId))
            Assertions.assertEquals(BigDecimal(100), limitOrderBook.getVolume(BigDecimal(20.0), BuyOrSellEnum.BUY))
        }

        @Test
        @DisplayName("Order volume should match bid/buy orders at price")
        fun orderVolumeShouldMatchBidBuyOrdersAtPrice() {
            val buyOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val buyOrder2 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(20.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )

            //when
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(buyOrder2)

            //then
            Assertions.assertEquals(2, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.BUY)?.orders?.size)
            Assertions.assertEquals(BigDecimal( 250), limitOrderBook.getVolume(BigDecimal(10.0), BuyOrSellEnum.BUY))
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
                price = BigDecimal(80.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val sellOrder2 = Order(
                price = BigDecimal(50.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )

            //when
            limitOrderBook.addOrder(sellOrder1)
            limitOrderBook.addOrder(sellOrder2)

            //then
            Assertions.assertEquals(BigDecimal(50.0), limitOrderBook.getBestAskOrNull()?.price)
            Assertions.assertEquals(sellOrder1, limitOrderBook.getOrderQueue(BigDecimal(80.0), BuyOrSellEnum.SELL)?.orders?.peek())
            Assertions.assertEquals(sellOrder2, limitOrderBook.getOrderByIdOrNull(sellOrder2.orderId))
            Assertions.assertEquals(BigDecimal(400.0), limitOrderBook.getVolume(BigDecimal(80.0), BuyOrSellEnum.SELL))
        }

        @Test
        @DisplayName("Order volume should match ask/sell orders at price")
        fun orderVolumeShouldMatchAskSellOrdersAtPrice() {
            //given
            val sellOrder1 = Order(
                price = BigDecimal(50.0),
                quantity = BigDecimal(10.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val sellOrder2 = Order(
                price = BigDecimal(50.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )

            //when
            limitOrderBook.addOrder(sellOrder1)
            limitOrderBook.addOrder(sellOrder2)

            //then
            Assertions.assertEquals(2, limitOrderBook.getOrderQueue(BigDecimal(50.0), BuyOrSellEnum.SELL)?.orders?.size)
            Assertions.assertEquals(BigDecimal(750.0), limitOrderBook.getVolume(BigDecimal(50.0), BuyOrSellEnum.SELL))
        }

    }



}
