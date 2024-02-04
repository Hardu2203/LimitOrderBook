package com.example.lob

import com.example.lob.order.BuyOrSellEnum
import com.example.lob.order.Order
import com.example.lob.trade.CurrencyPair
import org.junit.jupiter.api.*

class MatchBidTest {

    private lateinit var limitOrderBook: LimitOrderBook

    @BeforeEach
    fun setUp() {
        limitOrderBook = LimitOrderBook()
    }

    @Nested
    @DisplayName("Bid/Buy")
    inner class BidBuy {

        @Test
        @DisplayName("Bid should not match existing ask order at non-overlapping prices")
        fun bidShouldNotMatchExistingAskOrderAtNonOverlappingPrices() {
            //given
            val buyOrder1 = Order(
                price = 10.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val sellOrder1 = Order(
                price = 80.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )

            //when
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(sellOrder1)


            //then
            Assertions.assertEquals(10.0, limitOrderBook.getBestBidOrNull()?.price)
            Assertions.assertEquals(80.0, limitOrderBook.getBestAskOrNull()?.price)
            Assertions.assertEquals(buyOrder1, limitOrderBook.getOrderQueue(10.0, BuyOrSellEnum.BUY)?.orders?.peek())
            Assertions.assertEquals(sellOrder1, limitOrderBook.getOrderQueue(80.0, BuyOrSellEnum.SELL)?.orders?.peek())
            Assertions.assertEquals(50.0, limitOrderBook.getVolume(10.0, BuyOrSellEnum.BUY))
            Assertions.assertEquals(400.0, limitOrderBook.getVolume(80.0, BuyOrSellEnum.SELL))

            Assertions.assertEquals(0, limitOrderBook.getTradeHistory().size)
        }

        @Test
        @DisplayName("Bid should match existing ask order at same price")
        fun bidShouldMatchExistingAskOrder() {
            //given
            val sellOrder1 = Order(
                price = 10.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val buyOrder1 = Order(
                price = 10.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )

            //when
            limitOrderBook.addOrder(sellOrder1)
            limitOrderBook.addOrder(buyOrder1)


            //then
            Assertions.assertEquals(null, limitOrderBook.getBestBidOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(10.0, BuyOrSellEnum.BUY))
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(buyOrder1.orderId))
            Assertions.assertEquals(null, limitOrderBook.getVolume(10.0, BuyOrSellEnum.BUY))

            Assertions.assertEquals(null, limitOrderBook.getBestAskOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(10.0, BuyOrSellEnum.SELL))
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(sellOrder1.orderId))
            Assertions.assertEquals(null, limitOrderBook.getVolume(10.0, BuyOrSellEnum.SELL))

            Assertions.assertEquals(1, limitOrderBook.getTradeHistory().size)
        }

        @Test
        @DisplayName("Ask should match and have volume left on Bid")
        fun askShouldMatchAndHaveVolumeLeftOnBid() {
            //given
            val buyOrder1 = Order(
                price = 10.0,
                quantity = 20.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val sellOrder1 = Order(
                price = 10.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )

            //when
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(sellOrder1)

            //then
            Assertions.assertEquals(10.0, limitOrderBook.getBestBidOrNull()?.price)
            Assertions.assertEquals(1, limitOrderBook.getOrderQueue(10.0, BuyOrSellEnum.BUY)?.orders?.size)
            Assertions.assertEquals(150.0, limitOrderBook.getVolume(10.0, BuyOrSellEnum.BUY))

            Assertions.assertEquals(null, limitOrderBook.getBestAskOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(10.0, BuyOrSellEnum.SELL))
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(sellOrder1.orderId))
            Assertions.assertEquals(null, limitOrderBook.getVolume(10.0, BuyOrSellEnum.SELL))

            Assertions.assertEquals(1, limitOrderBook.getTradeHistory().size)
        }

        @Test
        @DisplayName("Bid should match and create bid")
        fun bidShouldMatchMultipleAsksAndCreateBid() {
            //given
            val sellOrder1 = Order(
                price = 10.0,
                quantity = 5.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val sellOrder2 = Order(
                price = 10.0,
                quantity = 2.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val buyOrder1 = Order(
                price = 10.0,
                quantity = 20.0,
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )


            //when
            limitOrderBook.addOrder(sellOrder1)
            limitOrderBook.addOrder(sellOrder2)
            limitOrderBook.addOrder(buyOrder1)


            //then
            Assertions.assertEquals(10.0, limitOrderBook.getBestBidOrNull()?.price)
            Assertions.assertEquals(1, limitOrderBook.getOrderQueue(10.0, BuyOrSellEnum.BUY)?.orders?.size)
            Assertions.assertEquals(130.0, limitOrderBook.getVolume(10.0, BuyOrSellEnum.BUY))

            Assertions.assertEquals(null, limitOrderBook.getBestAskOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(10.0, BuyOrSellEnum.SELL))
            Assertions.assertEquals(null, limitOrderBook.getVolume(10.0, BuyOrSellEnum.SELL))

            Assertions.assertEquals(2, limitOrderBook.getTradeHistory().size)
        }

    }

}
