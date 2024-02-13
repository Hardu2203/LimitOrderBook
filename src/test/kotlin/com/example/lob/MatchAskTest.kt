package com.example.lob

import com.example.lob.currencypair.CurrencyPair
import com.example.lob.limitorderbook.LimitOrderBook
import com.example.lob.order.BuyOrSellEnum
import com.example.lob.order.Order
import org.junit.jupiter.api.*
import java.math.BigDecimal

class MatchAskTest {

    private lateinit var limitOrderBook: LimitOrderBook

    @BeforeEach
    fun setUp() {
        limitOrderBook = LimitOrderBook()
    }

    @Nested
    @DisplayName("Ask/Sell")
    inner class AskSell {

        @Test
        @DisplayName("Sell should not match existing bid order at non-overlapping prices")
        fun askShouldNotMatchExistingBidOrderAtNonOverlappingPrices() {
            //given
            val sellOrder1 = Order(
                price = BigDecimal( 80.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val buyOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )


            //when
            limitOrderBook.addOrder(sellOrder1)
            limitOrderBook.addOrder(buyOrder1)

            //then
            Assertions.assertEquals(BigDecimal(10.0), limitOrderBook.getBestBidOrNull()?.price)
            Assertions.assertEquals(BigDecimal(80.0), limitOrderBook.getBestAskOrNull()?.price)
            Assertions.assertEquals(buyOrder1, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.BUY)?.orders?.peek())
            Assertions.assertEquals(sellOrder1, limitOrderBook.getOrderQueue(BigDecimal(80.0), BuyOrSellEnum.SELL)?.orders?.peek())

            Assertions.assertEquals(0, limitOrderBook.getTradeHistory().size)
        }

        @Test
        @DisplayName("Ask should match existing bid order at same price")
        fun askShouldMatchExistingBidOrder() {
            //given
            val buyOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val sellOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )

            //when
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(sellOrder1)


            //then
            Assertions.assertEquals(null, limitOrderBook.getBestBidOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.BUY))
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(buyOrder1.orderId))

            Assertions.assertEquals(null, limitOrderBook.getBestAskOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.SELL))
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(sellOrder1.orderId))

            Assertions.assertEquals(1, limitOrderBook.getTradeHistory().size)
            Assertions.assertEquals(BigDecimal(10), limitOrderBook.getTradeHistory().first().price)
        }

        @Test
        @DisplayName("Bid should match and have volume left on Ask")
        fun bidShouldMatchAndHaveVolumeLeftOnAsk() {
            //given
            val sellOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(20.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )
            val buyOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )

            //when
            limitOrderBook.addOrder(sellOrder1)
            limitOrderBook.addOrder(buyOrder1)


            //then
            Assertions.assertEquals(BigDecimal(10.0), limitOrderBook.getBestAskOrNull()?.price)
            Assertions.assertEquals(1, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.SELL)?.orders?.size)

            Assertions.assertEquals(null, limitOrderBook.getBestBidOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.BUY))
            Assertions.assertEquals(null, limitOrderBook.getOrderByIdOrNull(buyOrder1.orderId))

            Assertions.assertEquals(1, limitOrderBook.getTradeHistory().size)
            Assertions.assertEquals(BigDecimal(10), limitOrderBook.getTradeHistory().first().price)
        }

        @Test
        @DisplayName("Ask should match multiple bids and create ask")
        fun askShouldMatchMultipleBidsAndCreateBid() {
            //given
            val buyOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val buyOrder2 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(2.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val sellOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(20.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )


            //when
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(buyOrder2)
            limitOrderBook.addOrder(sellOrder1)


            //then
            Assertions.assertEquals(BigDecimal(10.0), limitOrderBook.getBestAskOrNull()?.price)
            Assertions.assertEquals(1, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.SELL)?.orders?.size)

            Assertions.assertEquals(null, limitOrderBook.getBestBidOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.BUY))

            Assertions.assertEquals(2, limitOrderBook.getTradeHistory().size)
        }

        @Test
        @DisplayName("Ask should match and trade at existing best bid price")
        fun askShouldMatchAndTradeAtExistingBestBidPrice() {
            //given
            val buyOrder1 = Order(
                price = BigDecimal(10.0),
                quantity = BigDecimal(50.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val buyOrder2 = Order(
                price = BigDecimal(15.0),
                quantity = BigDecimal(5.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Satoshi",
                buyOrSellEnum = BuyOrSellEnum.BUY
            )
            val sellOrder1 = Order(
                price = BigDecimal(8.0),
                quantity = BigDecimal(20.0),
                currencyPair = CurrencyPair.BTCZAR,
                username = "Vitalik",
                buyOrSellEnum = BuyOrSellEnum.SELL
            )


            //when
            limitOrderBook.addOrder(buyOrder1)
            limitOrderBook.addOrder(buyOrder2)
            limitOrderBook.addOrder(sellOrder1)


            //then
            Assertions.assertEquals(null, limitOrderBook.getBestAskOrNull())
            Assertions.assertEquals(null, limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.SELL))

            Assertions.assertEquals(BigDecimal(10.0), limitOrderBook.getBestBidOrNull()?.price)
            Assertions.assertEquals(BigDecimal(35.0), limitOrderBook.getOrderQueue(BigDecimal(10.0), BuyOrSellEnum.BUY)?.orders?.first?.quantity)

            Assertions.assertEquals(2, limitOrderBook.getTradeHistory().size)
            Assertions.assertEquals(BigDecimal(15.0), limitOrderBook.getTradeHistory().first().price)
            Assertions.assertEquals(BigDecimal(10.0), limitOrderBook.getTradeHistory()[1].price)
        }

    }

}
