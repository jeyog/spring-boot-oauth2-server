package com.jeyog.oauth2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OAuth2Application

fun main(args: Array<String>) {
    runApplication<OAuth2Application>(*args)
}
