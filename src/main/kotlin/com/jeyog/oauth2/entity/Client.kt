package com.jeyog.oauth2.entity

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Client(
        @Id
        val id: String,
        val clientId: String,
        val clientIdIssuedAt: Instant?,
        val clientSecret: String?,
        val clientSecretExpiresAt: Instant?,
        val clientName: String,
        @Column(length = 1000)
        val clientAuthenticationMethods: String,
        @Column(length = 1000)
        val authorizationGrantTypes: String,
        @Column(length = 1000)
        val redirectUris: String,
        @Column(length = 1000)
        val scopes: String,
        @Column(length = 2000)
        val clientSettings: String,
        @Column(length = 2000)
        val tokenSettings: String,
) {
}