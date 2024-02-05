package com.example.lob.limitorderbook

import com.example.lob.order.BuyOrSellEnum
import com.example.lob.order.Order
import com.example.lob.order.OrderId
import com.example.lob.trade.Trade
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.min

@Component
class LimitOrderBook(
    private val bestAsk: PriorityQueue<LimitPriceOrders> = PriorityQueue { o1, o2 -> o1.price.compareTo(o2.price) },
    private val bestBid: PriorityQueue<LimitPriceOrders> = PriorityQueue { o1, o2 -> o2.price.compareTo(o1.price) },
    private val orderMap: MutableMap<OrderId, Order> = mutableMapOf(),
    private val volumeMap: MutableMap<Pair<BigDecimal, BuyOrSellEnum>, BigDecimal> = mutableMapOf(),
    private val orderQueueMap: MutableMap<Pair<BigDecimal, BuyOrSellEnum>, LimitPriceOrders> = mutableMapOf(),
    private val recentTrades: TreeMap<ZonedDateTime, Trade> = TreeMap<ZonedDateTime, Trade>()
) {

    fun addOrder(order: Order) {
        val (sameSide, otherSide, sidePriceComparison) = getTradeSideDetail(order)

        while (order.volume > BigDecimal.ZERO && otherSide.isNotEmpty() && sidePriceComparison(
                otherSide.peek().orders.peek().price,
                order.price
            )
        ) {
            val otherLimitPriceOrders = otherSide.peek()
            val otherOrder = otherLimitPriceOrders.orders.peek()
            val tradePrice = otherOrder.price
            val tradeVolume = min(order.volume, otherOrder.volume)
            val tradeQuantity = min(order.quantity, otherOrder.quantity)

            reduceVolumeMap(otherOrder.price, otherOrder.buyOrSellEnum, tradeVolume)

            otherOrder.volume -= tradeVolume
            order.volume -= tradeVolume
            otherOrder.quantity -= tradeQuantity
            order.quantity -= tradeQuantity
            otherLimitPriceOrders.quantity -= tradeQuantity

            logger.info { "Existing order ${otherOrder.orderId} ${otherOrder.buyOrSellEnum} matched with ${order.orderId} ${order.buyOrSellEnum} " }

            addToTradeHistory(
                Trade(
                    price = tradePrice,
                    quantity = tradeQuantity,
                    currencyPair = otherOrder.currencyPair,
                    tradedAt = ZonedDateTime.now(),
                    takerSide = order.buyOrSellEnum,
                )
            )

            if (otherOrder.volume.compareTo(BigDecimal.ZERO) == 0) {
                cancelOrder(otherOrder.orderId)
            }
        }

        if (order.volume > BigDecimal.ZERO) {
            orderMap[order.orderId] = order
            addOrderToBook(order, sameSide)
        }
    }

    fun cancelOrder(
        orderId: OrderId,
    ) {
        val order = orderMap[orderId]
        if (order != null) {
            val limitPriceOrders = orderQueueMap[order.price to order.buyOrSellEnum]
                ?: throw IllegalStateException("OrderId $orderId was not found in the LimitOrderBook queueMap")
            limitPriceOrders.orders.remove(order)
            limitPriceOrders.quantity.minus(order.quantity)
            if (limitPriceOrders.orders.size == 0) {
                orderQueueMap.remove(order.price to order.buyOrSellEnum)
                val (sameSide) = getTradeSideDetail(order)
                sameSide.remove(limitPriceOrders)
            }
            if (order.volume > BigDecimal.ZERO) reduceVolumeMap(order.price, order.buyOrSellEnum, order.volume)
            orderMap.remove(orderId)
        }
    }

    fun getBestBidOrNull(): Order? {
        return bestBid.peek()?.orders?.peek()
    }

    fun getBestAskOrNull(): Order? {
        return bestAsk.peek()?.orders?.peek()
    }

    fun getOrderQueue(price: BigDecimal, buyOrSellEnum: BuyOrSellEnum): LimitPriceOrders? {
        return orderQueueMap[(price to buyOrSellEnum)]
    }

    fun getOrderByIdOrNull(orderId: OrderId): Order? {
        return orderMap[orderId]
    }

    fun getVolume(price: BigDecimal, buyOrSellEnum: BuyOrSellEnum): BigDecimal? {
        return volumeMap[(price to buyOrSellEnum)]
    }

    fun getTradeHistory(
        startTime: ZonedDateTime = ZonedDateTime.parse("2000-01-01T00:00:00Z"),
        endDateTime: ZonedDateTime = ZonedDateTime.parse("9999-12-31T23:59:59Z"),
        skip: Int = 0,
        limit: Int = 100,
    ): List<Trade> {
        return recentTrades.subMap(startTime, endDateTime).values.drop(skip).take(limit)
    }

    fun <T> getBestBids(first: Int, mapping: (LimitPriceOrders) -> T): List<T> {
        val minFirst = min(bestBid.size, first)
        val bestBidList: MutableList<T> = mutableListOf()
        repeat(minFirst) {
            bestBidList.add(bestBid.peek().let(mapping))
        }
        return bestBidList
    }

    fun <T> getBestAsks(first: Int, mapping: (LimitPriceOrders) -> T): List<T> {
        val minFirst = min(bestAsk.size, first)
        val bestAskList: MutableList<T> = mutableListOf()
        repeat(minFirst) {
            bestAskList.add(bestAsk.peek().let(mapping))
        }
        return bestAskList
    }

    private fun addToTradeHistory(trade: Trade) {
        logger.info { "Trade executed ${trade.currencyPair} ${trade.takerSide}, quantity ${trade.quantity} @ ${trade.price}" }
        recentTrades[trade.tradedAt] = trade
    }

    private fun reduceVolumeMap(orderPrice: BigDecimal, buyOrSellEnum: BuyOrSellEnum, volume: BigDecimal) {
        volumeMap.compute(orderPrice to buyOrSellEnum) { _, value ->
            val updatedVolume = value?.minus(volume)
            if (updatedVolume?.compareTo(BigDecimal.ZERO) == 0) null else updatedVolume
        }
    }

    private fun getTradeSideDetail(order: Order) = when (order.buyOrSellEnum) {
        BuyOrSellEnum.BUY -> Triple(bestBid, bestAsk) { a: BigDecimal, b: BigDecimal -> a <= b }
        BuyOrSellEnum.SELL -> Triple(bestAsk, bestBid) { a: BigDecimal, b: BigDecimal -> a >= b }
    }

    private fun addOrderToBook(
        order: Order,
        sameSide: PriorityQueue<LimitPriceOrders>,
    ) {
        logger.info { "Adding to orderbook: orderId ${order.orderId}, side ${order.buyOrSellEnum}, for ${order.quantity} @ ${order.price} " }
        val queue = orderQueueMap[order.price to order.buyOrSellEnum]
        if (queue != null) {
            queue.orders.add(order)
            queue.quantity += order.quantity
            volumeMap[order.price to order.buyOrSellEnum] =
                volumeMap[order.price to order.buyOrSellEnum]?.plus(order.volume)
                    ?: throw IllegalStateException("OrderId: ${order.orderId} was not found in the LimitOrderBook volumeMap")
        } else {
            val linkedList = LinkedList<Order>()
            linkedList.add(order)
            val limitPriceOrders = LimitPriceOrders(order.price, order.quantity, linkedList)
            sameSide.add(limitPriceOrders)
            orderQueueMap[order.price to order.buyOrSellEnum] = limitPriceOrders
            volumeMap[order.price to order.buyOrSellEnum] = order.volume
        }
    }

    fun min(a: BigDecimal, b: BigDecimal): BigDecimal {
        return if (a <= b) a else b
    }

}

private val logger = KotlinLogging.logger { }
