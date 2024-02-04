package com.example.lob.limitorderbook

import com.example.lob.order.OrderResponseDto
import com.fasterxml.jackson.annotation.JsonProperty

data class OrderBookDto(
    @JsonProperty("Asks")
    val asks: List<OrderResponseDto>,

    @JsonProperty("Bids")
    val bids: List<OrderResponseDto>
)
