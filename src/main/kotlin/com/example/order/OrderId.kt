package com.example.order

import java.util.concurrent.atomic.AtomicInteger

class OrderId {
    val id: Int = generateOrderId()
    companion object {
        private val id = AtomicInteger(0)

        fun generateOrderId(): Int {
            return id.incrementAndGet()
        }
    }

}
