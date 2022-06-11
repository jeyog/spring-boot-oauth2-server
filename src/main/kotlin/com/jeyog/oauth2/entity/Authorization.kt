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
        var authorizationCodeValue: String? = null,
        var authorizationCodeIssuedAt: Instant? = null,
        var authorizationCodeExpiresAt: Instant? = null,
        var authorizationCodeMetadata: String? = null,

        @Column(length = 4000)
        var accessTokenValue: String? = null,
        var accessTokenIssuedAt: Instant? = null,
        var accessTokenExpiresAt: Instant? = null,
        @Column(length = 2000)
        var accessTokenMetadata: String? = null,
        var accessTokenType: String? = null,
        @Column(length = 1000)
        var accessTokenScopes: String? = null,

        @Column(length = 4000)
        var refreshTokenValue: String? = null,
        var refreshTokenIssuedAt: Instant? = null,
        var refreshTokenExpiresAt: Instant? = null,
        @Column(length = 2000)
        var refreshTokenMetadata: String? = null,

        @Column(length = 4000)
        var oidcIdTokenValue: String? = null,
        var oidcIdTokenIssuedAt: Instant? = null,
        var oidcIdTokenExpiresAt: Instant? = null,
        @Column(length = 2000)
        var oidcIdTokenMetadata: String? = null,
        @Column(length = 2000)
        var oidcIdTokenClaims: String? = null
) {
}