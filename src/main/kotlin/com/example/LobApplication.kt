package com.example

import com.example.lob.LimitOrderBook
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.util.*

@SpringBootApplication
class LobApplication

fun main(args: Array<String>) {
    runApplication<LobApplication>(*args)
}
