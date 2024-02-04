package com.example.lob.security

import org.springframework.security.core.Authentication


interface AuthenticationFacade {
    fun getAuthentication(): Authentication
}
