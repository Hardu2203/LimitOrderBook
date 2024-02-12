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
    private val bestAsk: TreeMap<BigDecimal, LimitPriceOrders> = TreeMap(),
    private val bestBid: TreeMap<BigDecimal, LimitPriceOrders> = TreeMap(Collections.reverseOrder()),
    private val orderMap: MutableMap<OrderId, Order> = mutableMapOf(),
    private val volumeMap: MutableMap<Pair<BigDecimal, BuyOrSellEnum>, BigDecimal> = mutableMapOf(),
    private val orderQueueMap: MutableMap<Pair<BigDecimal, BuyOrSellEnum>, LimitPriceOrders> = mutableMapOf(),
    private val recentTrades: TreeMap<ZonedDateTime, Trade> = TreeMap<ZonedDateTime, Trade>()
) {

    fun addOrder(order: Order) {
        val (sameSide, otherSide, sidePriceComparison) = getTradeSideDetail(order)

        val orderWithRemainingVolume = matchOrders(order, otherSide, sidePriceComparison)

        if (orderWithRemainingVolume.quantity > BigDecimal.ZERO) {
            processRemainingOrder(orderWithRemainingVolume, sameSide)
        }
    }

    fun cancelOrder(
        orderId: OrderId,
    ) {
        val order = getOrderByOrderId(orderId)
        val orderQueueAtPriceAndSide = getOrderQueueAtPriceAndSide(order)
        orderQueueAtPriceAndSide.orders.remove(order)
        orderQueueAtPriceAndSide.quantity.minus(order.quantity)
        orderQueueAtPriceAndSide.orderCount = orderQueueAtPriceAndSide.orderCount.dec()
        if (orderQueueAtPriceAndSide.orders.size == 0) {
            removeOrderFromSameSideTreeMap(order, orderQueueAtPriceAndSide)
        }
        if (order.volume > BigDecimal.ZERO) reduceVolumeMap(order.price, order.buyOrSellEnum, order.volume)
        orderMap.remove(orderId)
    }

    fun getBestBidOrNull(): Order? {
        return if (bestBid.isEmpty()) {
            null
        } else {
            bestBid[bestBid.firstKey()]?.orders?.peek()
        }
    }

    fun getBestAskOrNull(): Order? {
        return if (bestAsk.isEmpty()) {
            null
        } else {
            bestAsk[bestAsk.firstKey()]?.orders?.peek()
        }
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
        val bestBidList: MutableList<T> = mutableListOf()

        bestBid.entries.take(min(bestBid.size, first)).forEach { entry ->
            entry.value.let { limitPriceOrders ->
                bestBidList.add(mapping(limitPriceOrders))
            }
        }

        return bestBidList
    }
    fun <T> getBestAsks(first: Int, mapping: (LimitPriceOrders) -> T): List<T> {
        val bestAskList: MutableList<T> = mutableListOf()

        bestAsk.entries.take(min(bestAsk.size, first)).forEach { entry ->
            entry.value.let { limitPriceOrders ->
                bestAskList.add(mapping(limitPriceOrders))
            }
        }

        return bestAskList
    }

    private fun matchOrders(
        order: Order,
        otherSide: TreeMap<BigDecimal, LimitPriceOrders>,
        sidePriceComparison: (BigDecimal, BigDecimal) -> Boolean
    ): Order {

        var remainingQuantity = order.quantity

        while (remainingQuantity > BigDecimal.ZERO && checkOtherSideBestPriceIsNotEmpty(otherSide) &&
            sidePriceComparison(getBestPrice(otherSide), order.price)) {

            val (bestMatchingPrice, bestLimitPriceOrders) = findBestMatchingPrice(otherSide)

            val tradeVolume = min(order.volume, bestLimitPriceOrders.orders.peek().volume)
            val tradeQuantity = min(order.quantity, bestLimitPriceOrders.orders.peek().quantity)

            executeTrade(order, bestLimitPriceOrders, tradeVolume, tradeQuantity)
            updateTradeHistory(order, bestMatchingPrice, order.buyOrSellEnum, tradeQuantity)

            remainingQuantity -= tradeQuantity
        }

        return order
    }

    private fun findBestMatchingPrice(otherSide: TreeMap<BigDecimal, LimitPriceOrders>): Pair<BigDecimal, LimitPriceOrders> {
        val bestPrice = otherSide.firstKey()
        val bestLimitPriceOrders = otherSide[bestPrice] ?: throw IllegalStateException("Best limit order price order should not be null")
        return bestPrice to bestLimitPriceOrders
    }

    private fun executeTrade(
        order: Order,
        bestLimitPriceOrders: LimitPriceOrders,
        tradeVolume: BigDecimal,
        tradeQuantity: BigDecimal
    ) {
        val otherOrder = bestLimitPriceOrders.orders.peek() ?: throw IllegalStateException("Best order should not be null")
        reduceVolumeMap(otherOrder.price, otherOrder.buyOrSellEnum, tradeVolume)

        otherOrder.volume -= tradeVolume
        order.volume -= tradeVolume
        otherOrder.quantity -= tradeQuantity
        order.quantity -= tradeQuantity
        bestLimitPriceOrders.quantity -= tradeQuantity

        logger.info { "Matching orders, id: ${order.orderId} (${order.buyOrSellEnum}) with id: ${otherOrder.orderId} (${otherOrder.buyOrSellEnum}), volume: $tradeVolume, quantity: $tradeQuantity" }

        if (otherOrder.quantity.compareTo(BigDecimal.ZERO) == 0) {
            cancelOrder(otherOrder.orderId)
        }
    }

    private fun updateTradeHistory(
        order: Order,
        tradePrice: BigDecimal,
        takerSide: BuyOrSellEnum,
        tradeQuantity: BigDecimal
    ) {
        val trade = Trade(
            price = tradePrice,
            quantity = tradeQuantity,
            currencyPair = order.currencyPair,
            tradedAt = ZonedDateTime.now(),
            takerSide = takerSide
        )
        recentTrades[trade.tradedAt] = trade
        logger.info { "Trade executed: ${trade.currencyPair} ${trade.takerSide}, quantity: $tradeQuantity @ ${trade.price}" }
    }

    private fun removeOrderFromSameSideTreeMap(
        order: Order,
        orderQueueAtPriceAndSide: LimitPriceOrders
    ) {
        orderQueueMap.remove(order.price to order.buyOrSellEnum)
        val (sameSide) = getTradeSideDetail(order)
        sameSide.remove(orderQueueAtPriceAndSide.price)
    }


    private fun processRemainingOrder(order: Order, sameSide: TreeMap<BigDecimal, LimitPriceOrders>) {
        orderMap[order.orderId] = order
        addOrderToBook(order, sameSide)
    }


    private fun getOrderQueueAtPriceAndSide(
        order: Order
    ) = (orderQueueMap[order.price to order.buyOrSellEnum]
        ?: throw NoSuchElementException("OrderId ${order.orderId} was not found in the LimitOrderBook queueMap"))


    private fun checkOtherSideBestPriceIsNotEmpty(otherSide: TreeMap<BigDecimal, LimitPriceOrders>): Boolean {
        return if (otherSide.isEmpty()) {
            false
        } else {
            val bestPrice = otherSide.firstKey()
            otherSide[bestPrice]?.orders?.peek()?.price != null
        }
    }

    private fun getBestPrice(otherSide: TreeMap<BigDecimal, LimitPriceOrders>): BigDecimal {
        return otherSide[otherSide.firstKey()]?.orders?.peek()?.price ?: throw IllegalStateException("Best price should not be null")
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
        sameSide: TreeMap<BigDecimal, LimitPriceOrders>,
    ) {
        logger.info { "Adding to orderbook: orderId ${order.orderId}, side ${order.buyOrSellEnum}, for ${order.quantity} @ ${order.price} " }
        val orderQueue = orderQueueMap[order.price to order.buyOrSellEnum]
        if (orderQueue != null) {
            addOrderToQueueAtPriceAndSide(orderQueue, order)
        } else {
            createQueueAtPriceAndSideWithOrder(order, sameSide)
        }
    }

    private fun createQueueAtPriceAndSideWithOrder(
        order: Order,
        sameSide: TreeMap<BigDecimal, LimitPriceOrders>
    ) {
        val linkedList = LinkedList<Order>()
        linkedList.add(order)
        val limitPriceOrders = LimitPriceOrders(order.price, order.quantity, linkedList)
        sameSide[order.price] = (limitPriceOrders)
        orderQueueMap[order.price to order.buyOrSellEnum] = limitPriceOrders
        volumeMap[order.price to order.buyOrSellEnum] = order.volume
    }

    private fun getOrderByOrderId(orderId: OrderId) =
        orderMap[orderId] ?: throw NoSuchElementException("OrderId: ${orderId.id} does not exist in the map")

    private fun addOrderToQueueAtPriceAndSide(orderQueue: LimitPriceOrders, order: Order) {
        orderQueue.orders.add(order)
        orderQueue.quantity += order.quantity
        orderQueue.orderCount = orderQueue.orderCount.inc()
        volumeMap[order.price to order.buyOrSellEnum] =
            volumeMap[order.price to order.buyOrSellEnum]?.plus(order.volume)
                ?: throw IllegalStateException("OrderId: ${order.orderId} was not found in the LimitOrderBook volumeMap")
    }

    private fun min(a: BigDecimal, b: BigDecimal): BigDecimal {
        return if (a <= b) a else b
    }

}

private val logger = KotlinLogging.logger { }
