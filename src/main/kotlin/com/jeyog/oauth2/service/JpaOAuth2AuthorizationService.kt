package com.jeyog.oauth2.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jeyog.oauth2.entity.Authorization
import com.jeyog.oauth2.repository.AuthorizationRepository
import com.jeyog.oauth2.repository.JpaRegisteredClientRepository
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AuthorizationCode
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.core.OAuth2TokenType
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import org.springframework.util.StringUtils
import java.time.Instant
import java.util.function.Consumer


@Component
class JpaOAuth2AuthorizationService(
    private val authorizationRepository: AuthorizationRepository,
    private val registeredClientRepository: JpaRegisteredClientRepository
): OAuth2AuthorizationService {
    private val objectMapper = ObjectMapper()

    init {
        val classLoader = JpaOAuth2AuthorizationService::class.java.classLoader
        val securityModules= SecurityJackson2Modules.getModules(classLoader)
        objectMapper.registerModules(securityModules)
        objectMapper.registerModule(OAuth2AuthorizationServerJackson2Module())
    }

    override fun save(authorization: OAuth2Authorization?) {
        Assert.notNull(authorization, "authorization cannot be null")
        this.authorizationRepository.save(toEntity(authorization!!))
    }

    override fun remove(authorization: OAuth2Authorization?) {
        Assert.notNull(authorization, "authorization cannot be null")
        this.authorizationRepository.deleteById(authorization!!.id)
    }

    override fun findById(id: String?): OAuth2Authorization? {
        Assert.hasText(id, "id cannot be empty")
        return this.authorizationRepository.findById(id!!).map(this::toObject).orElse(null)
    }

    override fun findByToken(token: String?, tokenType: OAuth2TokenType?): OAuth2Authorization? {
        Assert.hasText(token, "token cannot be empty")

        val result: Authorization? = if (tokenType == null) {
            this.authorizationRepository.findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValue(token!!)
        } else if (OAuth2ParameterNames.STATE == tokenType.value) {
            this.authorizationRepository.findByState(token!!)
        } else if (OAuth2ParameterNames.CODE == tokenType.value) {
            this.authorizationRepository.findByAuthorizationCodeValue(token!!)
        } else if (OAuth2ParameterNames.ACCESS_TOKEN == tokenType.value) {
            this.authorizationRepository.findByAccessTokenValue(token!!)
        } else if (OAuth2ParameterNames.REFRESH_TOKEN == tokenType.value) {
            this.authorizationRepository.findByRefreshTokenValue(token!!)
        } else {
            null
        }

        return result?.let { toObject(it) }
    }

    private fun toObject(entity: Authorization): OAuth2Authorization {
        val registeredClient = this.registeredClientRepository.findById(entity.registeredClientId)
            ?: throw DataRetrievalFailureException("The RegisteredClient with id '${entity.registeredClientId}' was not found in the RegisteredClientRepository.")

        val builder = OAuth2Authorization.withRegisteredClient(registeredClient)
            .id(entity.id)
            .principalName(entity.principalName)
            .authorizationGrantType(resolveAuthorizationGrantType(entity.authorizationGrantType))
            .attributes { attributes -> attributes.putAll(parseMap(entity.attributes!!)) }

        if (entity.authorizationCodeValue != null) {
            val authorizationCode = OAuth2AuthorizationCode(
                entity.authorizationCodeValue!!,
                entity.authorizationCodeIssuedAt,
                entity.authorizationCodeExpiresAt
            )
            builder.token(authorizationCode) { metadata ->
                metadata.putAll(parseMap(entity.authorizationCodeMetadata!!))
            }
        }

        if (entity.accessTokenValue != null) {
            val accessToken = OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                entity.accessTokenValue,
                entity.accessTokenIssuedAt,
                entity.accessTokenExpiresAt,
                StringUtils.commaDelimitedListToSet(entity.accessTokenScopes)
            )
            builder.token(accessToken) { metadata ->
                metadata.putAll(parseMap(entity.accessTokenMetadata!!))
            }
        }

        if (entity.refreshTokenValue != null) {
            val refreshToken = OAuth2RefreshToken(
                entity.refreshTokenValue,
                entity.refreshTokenIssuedAt,
                entity.refreshTokenExpiresAt
            )
            builder.token(refreshToken) { metadata ->
                metadata.putAll(parseMap(entity.refreshTokenMetadata!!))
            }
        }

        if (entity.oidcIdTokenValue != null) {
            val idToken = OidcIdToken(
                entity.oidcIdTokenValue,
                entity.oidcIdTokenIssuedAt,
                entity.oidcIdTokenExpiresAt,
                parseMap(entity.oidcIdTokenClaims!!)
            )
            builder.token(idToken) { metadata ->
                metadata.putAll(parseMap(entity.oidcIdTokenMetadata!!))
            }
        }

        return builder.build()
    }

    private fun toEntity(authorization: OAuth2Authorization): Authorization {
        val entity = Authorization(
            id = authorization.id,
            registeredClientId = authorization.registeredClientId,
            principalName = authorization.principalName,
            authorizationGrantType = authorization.authorizationGrantType.value,
            attributes = writeMap(authorization.attributes),
            state = authorization.getAttribute(OAuth2ParameterNames.STATE)
        )

        val authorizationCode = authorization.getToken(OAuth2AuthorizationCode::class.java)
        setTokenValues(
            authorizationCode,
            entity::authorizationCodeValue::set,
            entity::authorizationCodeIssuedAt::set,
            entity::authorizationCodeExpiresAt::set,
            entity::authorizationCodeMetadata::set
        )

        val accessToken = authorization.getToken(OAuth2AccessToken::class.java)
        setTokenValues(
            accessToken,
            entity::accessTokenValue::set,
            entity::accessTokenIssuedAt::set,
            entity::accessTokenExpiresAt::set,
            entity::accessTokenMetadata::set
        )
        if (accessToken != null && accessToken.token.scopes != null) {
            entity.accessTokenScopes = StringUtils.collectionToDelimitedString(accessToken.token.scopes, ",")
        }

        val refreshToken = authorization.getToken(OAuth2RefreshToken::class.java)
        setTokenValues(
            refreshToken,
            entity::refreshTokenValue::set,
            entity::refreshTokenIssuedAt::set,
            entity::refreshTokenExpiresAt::set,
            entity::refreshTokenMetadata::set
        )

        val oidcIdToken = authorization.getToken(OidcIdToken::class.java)
        setTokenValues(
            oidcIdToken,
            entity::oidcIdTokenValue::set,
            entity::oidcIdTokenIssuedAt::set,
            entity::oidcIdTokenExpiresAt::set,
            entity::oidcIdTokenMetadata::set
        )
        if (oidcIdToken != null) {
            entity.oidcIdTokenClaims = writeMap(oidcIdToken.claims!!)
        }

        return entity
    }

    private fun setTokenValues(
        token: OAuth2Authorization.Token<*>?,
        tokenValueConsumer: Consumer<String>,
        issuedAtConsumer: Consumer<Instant>,
        expiresAtConsumer: Consumer<Instant>,
        metadataConsumer: Consumer<String>
    ) {
        if (token != null) {
            val oAuth2Token = token.token
            tokenValueConsumer.accept(oAuth2Token.tokenValue)
            issuedAtConsumer.accept(oAuth2Token.issuedAt!!)
            expiresAtConsumer.accept(oAuth2Token.expiresAt!!)
            metadataConsumer.accept(writeMap(token.metadata))
        }
    }

    private fun parseMap(data: String): Map<String, Any> {
        return try {
            this.objectMapper.readValue(data, object: TypeReference<Map<String, Any>> () {})
        } catch (ex: Exception) {
            throw IllegalArgumentException(ex.message, ex)
        }
    }

    private fun writeMap(metadata: Map<String, Any>): String {
        return try {
            this.objectMapper.writeValueAsString(metadata)
        } catch (ex: Exception) {
            throw IllegalArgumentException(ex.message, ex)
        }
    }
}

private fun resolveAuthorizationGrantType(authorizationGrantType: String): AuthorizationGrantType {
    return if (AuthorizationGrantType.AUTHORIZATION_CODE.value == authorizationGrantType) {
        AuthorizationGrantType.AUTHORIZATION_CODE
    } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.value == authorizationGrantType) {
        AuthorizationGrantType.CLIENT_CREDENTIALS
    } else if (AuthorizationGrantType.REFRESH_TOKEN.value == authorizationGrantType) {
        AuthorizationGrantType.REFRESH_TOKEN
    } else {
        AuthorizationGrantType(authorizationGrantType)
    }
}