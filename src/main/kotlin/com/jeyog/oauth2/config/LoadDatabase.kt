package com.jeyog.oauth2.config

import com.jeyog.oauth2.entity.User
import com.jeyog.oauth2.entity.UserRole
import com.jeyog.oauth2.repository.JpaRegisteredClientRepository
import com.jeyog.oauth2.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.OidcScopes
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.config.ClientSettings

@Configuration
class LoadDatabase(
    private val passwordEncoder: PasswordEncoder
) {
    @Bean
    fun initClientDatabase(repository: JpaRegisteredClientRepository) = CommandLineRunner {
        val registeredClient = RegisteredClient.withId("78dc002d-a906-4b9b-ba82-9abf31f14dbf")
                .clientId("G7s6BIEKoXXabzCkxqpE")
                .clientSecret(passwordEncoder.encode("secret"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc")
                .redirectUri("http://127.0.0.1:8080/authorized")
                .scope(OidcScopes.OPENID)
                .scope("message.read")
                .scope("message.write")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build()
        repository.save(registeredClient)
    }

    @Bean
    fun initUserDatabase(repository: UserRepository) = CommandLineRunner {
        val user = User(
            id = 1,
            username = "test",
            password = passwordEncoder.encode("1234qwer@"),
            roles = arrayListOf(UserRole.ROLE_USER)
        )
        repository.save(user)
    }
}