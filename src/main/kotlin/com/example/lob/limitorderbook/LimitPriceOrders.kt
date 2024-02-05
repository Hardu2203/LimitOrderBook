package com.example.lob.limitorderbook

import com.example.lob.order.Order
import java.math.BigDecimal
import java.util.*

data class LimitPriceOrders(
    val price: BigDecimal,
    var quantity: BigDecimal,
    val orders: LinkedList<Order>,
    var orderCount: Int = orders.size
)
