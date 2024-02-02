package com.example.lob

import com.example.order.BuyOrSellEnum
import com.example.order.Order
import com.example.order.OrderId
import com.example.trade.Trade
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.min

@Component
class LimitOrderBook(
    private val bestAsk: PriorityQueue<LimitPriceOrders> = PriorityQueue { o1, o2 -> o2.price.compareTo(o1.price) },
    private val bestBid: PriorityQueue<LimitPriceOrders> = PriorityQueue { o1, o2 -> o1.price.compareTo(o2.price) },
    private val orderMap: MutableMap<OrderId, Order> = mutableMapOf(),
    private val volumeMap: MutableMap<Pair<Double, BuyOrSellEnum>, Double> = mutableMapOf(),
    private val queueMap: MutableMap<Pair<Double, BuyOrSellEnum>, LimitPriceOrders> = mutableMapOf(),
    private val recentTrades: TreeMap<ZonedDateTime, Trade> = TreeMap()
) {

    fun addOrder(order: Order) {
        val (sameSide, otherSide, priceComparison) = when (order.buyOrSellEnum) {
            BuyOrSellEnum.BUY -> Triple(bestBid, bestAsk) { a: Double, b: Double -> a <= b }
            BuyOrSellEnum.SELL -> Triple(bestAsk, bestBid) { a: Double, b: Double -> a >= b }
        }

        while (order.volume > 0 && otherSide.isNotEmpty() && priceComparison(
                otherSide.peek().orders.peek().price,
                order.price
            )
        ) {
            val otherOrder = otherSide.peek().orders.peek()
            val tradePrice = otherOrder.price
            val tradeVolume = min(order.volume, otherOrder.volume)
            otherOrder.volume = otherOrder.volume.minus(tradeVolume)
            order.volume = order.volume.minus(tradeVolume)

            logger.info { "${order.currencyPair} ${order.buyOrSellEnum} order placed by ${order.username}, taken by ${otherOrder.username}, volume $tradeVolume @ $tradePrice" }

            if (otherOrder.volume == 0.0) {
                cancelOrder(otherOrder.orderId, sameSide)
            } else {
                orderMap[order.orderId] = order
                addOrderToBook(order, sameSide)
            }
        }
    }

    private fun addOrderToBook(
        order: Order,
        sameSide: PriorityQueue<LimitPriceOrders>,
    ) {

        val queue = queueMap[order.price to order.buyOrSellEnum]
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
            queueMap[order.price to order.buyOrSellEnum] = limitPriceOrders
            volumeMap[order.price to order.buyOrSellEnum] = order.volume
        }
    }

    private fun cancelOrder(
        orderId: OrderId,
        sameSide: PriorityQueue<LimitPriceOrders>,
    ) {
        val order = orderMap[orderId]
        if (order != null) {
            val limitPriceOrders = queueMap[order.price to order.buyOrSellEnum]
                ?: throw IllegalStateException("OrderId $orderId was not found in the LimitOrderBook queueMap")
            limitPriceOrders.orders.remove(order)
            if (limitPriceOrders.orders.size == 0) {
                sameSide.remove(limitPriceOrders)
            }
            volumeMap[order.price to order.buyOrSellEnum] =
                volumeMap[order.price to order.buyOrSellEnum]?.minus(order.volume)
                    ?: throw IllegalStateException("OrderId: ${order.orderId} was not found in the LimitOrderBook volumeMap")
        }
    }

}

private val logger = KotlinLogging.logger { }
