package com.example.lob.currencypair

import com.example.lob.BaseController
import com.example.lob.limitorderbook.LimitOrderBookService
import com.example.lob.trade.Trade
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime

@RestController
@RequestMapping(BaseController.CURRENCY_PAIR_TRADE_HISTORY_ROOT_URL)
class CurrencyPairTradeHistoryController(
    private val limitOrderBookService: LimitOrderBookService
) {

    @GetMapping
    fun getTradeHistory(
        @PathVariable pair: CurrencyPair,
        @RequestParam startTime: ZonedDateTime = ZonedDateTime.parse("2017-01-01T00:00:00Z"),
        @RequestParam endDateTime: ZonedDateTime = ZonedDateTime.parse("9999-12-31T23:59:59Z"),
        @RequestParam skip: Int = 0,
        @RequestParam limit: Int = 100,
    ): List<Trade> {
        val validatedLimit = if (limit > 100) 100 else limit
        return limitOrderBookService.getTradeHistory(pair, startTime, endDateTime, skip, validatedLimit)
    }

}
