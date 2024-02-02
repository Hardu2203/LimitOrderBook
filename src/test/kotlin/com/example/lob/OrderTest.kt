package com.example.lob

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderTest {

    private lateinit var limitOrderBookService: LimitOrderBookService

    @BeforeEach
    fun setUp() {
        limitOrderBookService = LimitOrderBookService(LimitOrderBook())
    }

    @Test
    fun addOrder() {
        limitOrderBookService.addOrder()

    }

}
