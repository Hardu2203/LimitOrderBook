package com.example.lob

import com.example.lob.order.BuyOrSellEnum
import com.example.lob.order.Order
import com.example.lob.order.OrderId
import com.example.lob.trade.Trade
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.min

@Component
class LimitOrderBook(
    val bestAsk: PriorityQueue<LimitPriceOrders> = PriorityQueue { o1, o2 -> o1.price.compareTo(o2.price) },
    val bestBid: PriorityQueue<LimitPriceOrders> = PriorityQueue { o1, o2 -> o2.price.compareTo(o1.price) },
    val orderMap: MutableMap<OrderId, Order> = mutableMapOf(),
    val volumeMap: MutableMap<Pair<Double, BuyOrSellEnum>, Double> = mutableMapOf(),
    val orderQueueMap: MutableMap<Pair<Double, BuyOrSellEnum>, LimitPriceOrders> = mutableMapOf(),
    val recentTrades: TreeMap<ZonedDateTime, Trade> = TreeMap()
) {

    fun addOrder(order: Order) {
        val (sameSide, otherSide, sidePriceComparison) = getTradeSideDetail(order)

        while (order.volume > 0 && otherSide.isNotEmpty() && sidePriceComparison(
                otherSide.peek().orders.peek().price,
                order.price
            )
        ) {
            val otherOrder = otherSide.peek().orders.peek()
            val tradePrice = otherOrder.price
            val tradeVolume = min(order.volume, otherOrder.volume)
            reduceVolumeMap(otherOrder.price, otherOrder.buyOrSellEnum, tradeVolume)
            otherOrder.volume = otherOrder.volume.minus(tradeVolume)
            order.volume = order.volume.minus(tradeVolume)

            logger.info { "Existing order ${otherOrder.orderId} ${otherOrder.buyOrSellEnum} matched with ${order.orderId} ${order.buyOrSellEnum} " }

            addToTradeHistory(
                Trade(
                    price = tradePrice,
                    quantity = min(order.quantity, otherOrder.quantity),
                    currencyPair = otherOrder.currencyPair,
                    tradedAt = ZonedDateTime.now(),
                    takerSide = order.buyOrSellEnum,
                )
            )

            if (otherOrder.volume == 0.0) {
                cancelOrder(otherOrder.orderId)
            }
        }

        if (order.volume > 0) {
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
            if (limitPriceOrders.orders.size == 0) {
                orderQueueMap.remove(order.price to order.buyOrSellEnum)
                val (sameSide) = getTradeSideDetail(order)
                sameSide.remove(limitPriceOrders)
            }
            if (order.volume > 0) reduceVolumeMap(order.price, order.buyOrSellEnum, order.volume)
            orderMap.remove(orderId)
        }
    }

    fun getBestBidOrNull(): Order? {
        return bestBid.peek()?.orders?.peek()
    }

    fun getBestAskOrNull(): Order? {
        return bestAsk.peek()?.orders?.peek()
    }

    fun getOrderQueue(price: Double, buyOrSellEnum: BuyOrSellEnum): LimitPriceOrders? {
        return orderQueueMap[(price to buyOrSellEnum)]
    }

    fun getOrderByIdOrNull(orderId: OrderId): Order? {
        return orderMap[orderId]
    }

    fun getVolume(price: Double, buyOrSellEnum: BuyOrSellEnum): Double? {
        return volumeMap[(price to buyOrSellEnum)]
    }

    fun getTradeHistory(): TreeMap<ZonedDateTime, Trade> {
        return TreeMap(recentTrades)
    }

    private fun addToTradeHistory(trade: Trade) {
        logger.info { "Trade executed ${trade.currencyPair} ${trade.takerSide}, quantity ${trade.quoteVolume} @ ${trade.price}" }
        recentTrades[trade.tradedAt] = trade
    }

    private fun reduceVolumeMap(orderPrice: Double, buyOrSellEnum: BuyOrSellEnum, volume: Double) {
        volumeMap.compute(orderPrice to buyOrSellEnum) { _, value ->
            val updatedVolume = value?.minus(volume)
            if (updatedVolume == 0.0) null else updatedVolume
        }
    }

    private fun getTradeSideDetail(order: Order) = when (order.buyOrSellEnum) {
        BuyOrSellEnum.BUY -> Triple(bestBid, bestAsk) { a: Double, b: Double -> a <= b }
        BuyOrSellEnum.SELL -> Triple(bestAsk, bestBid) { a: Double, b: Double -> a >= b }
    }

    private fun addOrderToBook(
        order: Order,
        sameSide: PriorityQueue<LimitPriceOrders>,
    ) {
        logger.info { "Adding to orderbook: orderId ${order.orderId}, side ${order.buyOrSellEnum}, for ${order.quantity} @ ${order.price} " }
        val queue = orderQueueMap[order.price to order.buyOrSellEnum]
        if (queue != null) {
            queue.orders.add(order)
            volumeMap[order.price to order.buyOrSellEnum] =
                volumeMap[order.price to order.buyOrSellEnum]?.plus(order.volume)
                    ?: throw IllegalStateException("OrderId: ${order.orderId} was not found in the LimitOrderBook volumeMap")
        } else {
            val linkedList = LinkedList<Order>()
            linkedList.add(order)
            val limitPriceOrders = LimitPriceOrders(order.price, linkedList)
            sameSide.add(limitPriceOrders)
            orderQueueMap[order.price to order.buyOrSellEnum] = limitPriceOrders
            volumeMap[order.price to order.buyOrSellEnum] = order.volume
        }
    }

}

private val logger = KotlinLogging.logger { }
