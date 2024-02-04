package com.example.lob.limitorderbook

import com.example.lob.order.Order
import java.util.*

data class LimitPriceOrders(
    val price: Double,
    var quantity: Double,
    val orders: LinkedList<Order>,
    val orderCount: Int = orders.size
)
