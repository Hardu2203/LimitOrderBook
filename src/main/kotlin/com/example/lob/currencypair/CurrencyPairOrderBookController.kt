package com.example.lob.currencypair

import com.example.lob.BaseController
import com.example.lob.limitorderbook.LimitOrderBookService
import com.example.lob.limitorderbook.OrderBookDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BaseController.CURRENCY_PAIR_ORDER_ROOT_URL)
class CurrencyPairOrderBookController(
    private val limitOrderBookService: LimitOrderBookService
) {

    @GetMapping
    fun getOrderBook(@PathVariable pair: CurrencyPair): OrderBookDto {
        return limitOrderBookService.getOrderBook(pair)
    }

}
