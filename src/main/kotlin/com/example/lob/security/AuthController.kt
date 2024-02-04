package com.example.lob.security

import mu.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class AuthController(private val tokenService: TokenService) {
    @PostMapping("/token")
    fun token(authentication: Authentication): String {
        logger.info { "Token requested for user: ${authentication.name}" }
        return tokenService.generateToken(authentication)
    }

}

private val logger = KotlinLogging.logger {  }
