package com.jeyog.oauth2.entity

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Authorization(
        @Id
        val id: String,
        val registeredClientId: String,
        val principalName: String,
        val authorizationGrantType: String,
        @Column(length = 4000)
        val attributes: String,
        @Column(length = 500)
        val state: String,

        @Column(length = 4000)
        val authorizationCodeValue: String,
        val authorizationCodeIssuedAt: Instant,
        val authorizationCodeExpiresAt: Instant,
        val authorizationCodeMetadata: String,

        @Column(length = 4000)
        val accessTokenValue: String,
        val accessIssuedAt: Instant,
        val accessTokenExpiresAt: Instant,
        @Column(length = 2000)
        val accessTokenMetadata: String,
        val accessTokenType: String,
        @Column(length = 1000)
        val accessTokenScopes: String,

        @Column(length = 4000)
        val refreshTokenValue: String,
        val refreshTokenIssuedAt: Instant,
        val refreshTokenExpiresAt: Instant,
        @Column(length = 2000)
        val refreshTokenMetadata: String,

        @Column(length = 4000)
        val oidcIdTokenValue: String,
        val oidcIdTokenIssuedAt: Instant,
        val oidcIdTokenExpiresAt: Instant,
        @Column(length = 2000)
        val oidcIdTokenMetadata: String,
        @Column(length = 2000)
        val oidcIdTokenClaims: String
) {
}