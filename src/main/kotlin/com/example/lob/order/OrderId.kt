package com.example.lob.order

import java.util.concurrent.atomic.AtomicInteger

data class OrderId(val id: Int = generateOrderId()) {

    companion object {
        private val id = AtomicInteger(0)

        fun generateOrderId(): Int {
            return id.incrementAndGet()
        }
    }

    override fun toString(): String {
        return "$id"
    }
}
