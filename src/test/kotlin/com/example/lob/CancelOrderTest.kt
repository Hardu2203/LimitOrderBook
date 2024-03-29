package com.example.lob

import com.example.lob.currencypair.CurrencyPair
import com.example.lob.limitorderbook.LimitOrderBook
import com.example.lob.order.BuyOrSellEnum
import com.example.lob.order.Order
import org.junit.jupiter.api.*
import java.math.BigDecimal

class CancelOrderTest {

    private lateinit var limitOrderBook: LimitOrderBook

    @BeforeEach
    fun setUp() {
        limitOrderBook = LimitOrderBook()
    }

    @Nested
    @DisplayName("Bid/Buy")
    inner class BidBuy {

        @Test
        @DisplayName("Cancel should remove order from orderQueueMap")
        fun cancelShouldRemoveFromOrderQueueMap() {
            //given
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
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(buyOrder2)
            Assertions.assertEquals(BigDecimal(10), limitOrderBook.getBestBidOrNull()?.price)
            Assertions.assertEquals(2, limitOrderBook.getOrderQueue(BigDecimal(10), BuyOrSellEnum.BUY)?.orders?.size)

            //when
            limitOrderBook.cancelOrder(buyOrder1.orderId)

            //then
            Assertions.assertEquals(buyOrder2.orderId, limitOrderBook.getBestBidOrNull()?.orderId)
            Assertions.assertEquals(1, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.BUY)?.orders?.size)
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(buyOrder1.orderId))
        }


        @Test
        @DisplayName("Cancel should remove order from orderQueueMap and remove node from bestBid")
        fun cancelShouldRemoveFromOrderQueueMapAndRemoveNodeFromBestBid() {
            //given
            val buyOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            limitOrderBook.addOrder(buyOrder1)
            Assertions.assertEquals(BigDecimal(10.0), limitOrderBook.getBestBidOrNull()?.price)

            //when
            limitOrderBook.cancelOrder(buyOrder1.orderId)

            //then
            Assertions.assertEquals(null, limitOrderBook.getBestBidOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(BigDecimal(10), BuyOrSellEnum.BUY))
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(buyOrder1.orderId))
        }
    }

    @Nested
    @DisplayName("Ask/Sell")
    inner class AskSell {

        @Test
        @DisplayName("Cancel should remove order from orderQueueMap")
        fun cancelShouldRemoveFromOrderQueueMap() {
            //given
            val sellOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val sellOrder2 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(20.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            limitOrderBook.addOrder(sellOrder1)
            limitOrderBook.addOrder(sellOrder2)
            Assertions.assertEquals(BigDecimal(10.0), limitOrderBook.getBestAskOrNull()?.price)
            Assertions.assertEquals(2, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.SELL)?.orders?.size)

            //when
            limitOrderBook.cancelOrder(sellOrder1.orderId)

            //then
            Assertions.assertEquals(sellOrder2.orderId, limitOrderBook.getBestAskOrNull()?.orderId)
            Assertions.assertEquals(1, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.SELL)?.orders?.size)
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(sellOrder1.orderId))
        }


        @Test
        @DisplayName("Cancel should remove order from orderQueueMap and remove node from bestBid")
        fun cancelShouldRemoveFromOrderQueueMapAndRemoveNodeFromBestBid() {
            //given
            val sellOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            limitOrderBook.addOrder(sellOrder1)
            Assertions.assertEquals(BigDecimal(10.0), limitOrderBook.getBestAskOrNull()?.price)

            //when
            limitOrderBook.cancelOrder(sellOrder1.orderId)

            //then
            Assertions.assertEquals(null, limitOrderBook.getBestAskOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.SELL))
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(sellOrder1.orderId))
        }
    }

}
