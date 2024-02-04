package com.example

import com.example.lob.security.RsaKeyProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties::class)
class LobApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<LobApplication>(*args)
}
