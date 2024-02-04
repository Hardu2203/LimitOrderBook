package com.example.lob


class BaseController {

    companion object {
        private const val API_ROOT_URL = "/api"
        private const val API_V1_ROOT_URL = "$API_ROOT_URL/v1"

        const val ORDER_ROOT_URL = "$API_V1_ROOT_URL/orders/limit"
        const val CURRENCY_PAIR_ORDER_ROOT_URL = "$API_V1_ROOT_URL/{pair}/orderbook"
        const val CURRENCY_PAIR_TRADE_HISTORY_ROOT_URL = "$API_V1_ROOT_URL/{pair}/tradehistory"

    }
}
