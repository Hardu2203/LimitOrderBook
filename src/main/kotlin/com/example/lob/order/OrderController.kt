package com.example.lob.order

import com.example.lob.BaseController
import com.example.lob.limitorderbook.LimitOrderBookService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BaseController.ORDER_ROOT_URL)
class OrderController(
    val limitOrderBookService: LimitOrderBookService
) {
    @PostMapping
    fun createLimitOrder(
        @RequestBody orderDto: OrderDto
    ) {
        limitOrderBookService.addOrder(orderDto)
    }

}
