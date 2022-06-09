package com.jeyog.oauth2.config

import org.springframework.context.annotation.Bean
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain


@EnableWebSecurity
class DefaultSecurityConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    @Throws(Exception::class)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .authorizeRequests {
                authorizeRequests -> authorizeRequests.anyRequest().authenticated()
            }
            .formLogin(withDefaults())
        return http.build()
    }

    @Bean
    fun users(): UserDetailsService {
        val user = User.builder()
                .username("user1")
                .password("\$2a\$10\$8cv4lq9gi2AaLPa9skKCcumHkqjCzH1hU1KEr7/7wUkyrScOQFiOG")
                .roles("USER")
                .build()
        return InMemoryUserDetailsManager(user)
    }
}