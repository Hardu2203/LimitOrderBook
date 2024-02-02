package com.example.lob

import com.example.order.Order
import java.util.LinkedList

data class LimitPriceOrders(
    val price: Double,
    val orders: LinkedList<Order>
)
