package com.jeyog.oauth2.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jeyog.oauth2.entity.Client
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.ClientSettings
import org.springframework.security.oauth2.server.authorization.config.TokenSettings
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import org.springframework.util.StringUtils


@Component
class JpaRegisteredClientRepository(
    private val clientRepository: ClientRepository
): RegisteredClientRepository {
    private val objectMapper = ObjectMapper()

    init {
        val classLoader = JpaRegisteredClientRepository::class.java.classLoader
        val securityModules = SecurityJackson2Modules.getModules(classLoader)
        objectMapper.registerModules(securityModules)
        objectMapper.registerModule(OAuth2AuthorizationServerJackson2Module())
    }

    override fun save(registeredClient: RegisteredClient?) {
        Assert.notNull(registeredClient, "registeredClient cannot be null")
        this.clientRepository.save(toEntity(registeredClient!!))
    }

    override fun findById(id: String?): RegisteredClient? {
        Assert.hasText(id, "id cannot be empty")
        return this.clientRepository.findById(id!!).map(this::toObject).orElse(null)
    }

    override fun findByClientId(clientId: String?): RegisteredClient? {
        Assert.hasText(clientId, "clientId cannot be empty")
        return this.clientRepository.findByClientId(clientId!!)?.let { toObject(it) }
    }

    private fun toObject(client: Client): RegisteredClient {
        val clientAuthenticationMethods = StringUtils.commaDelimitedListToSet(
            client.clientAuthenticationMethods
        )
        val authorizationGrantTypes = StringUtils.commaDelimitedListToSet(
            client.authorizationGrantTypes
        )
        val redirectUris = StringUtils.commaDelimitedListToSet(
            client.redirectUris
        )
        val clientScopes = StringUtils.commaDelimitedListToSet(
            client.scopes
        )

        val builder = RegisteredClient.withId(client.id)
            .clientId(client.clientId)
            .clientIdIssuedAt(client.clientIdIssuedAt)
            .clientSecret(client.clientSecret)
            .clientSecretExpiresAt(client.clientSecretExpiresAt)
            .clientName(client.clientName)
            .clientAuthenticationMethods { authenticationMethods ->
                clientAuthenticationMethods.forEach { authenticationMethod ->
                    authenticationMethods.add(resolveClientAuthenticationMethod(authenticationMethod))
                }
            }
            .authorizationGrantTypes { grantTypes ->
                authorizationGrantTypes.forEach { grantType ->
                    grantTypes.add(resolveAuthorizationGrantType(grantType))
                }
            }
            .redirectUris { uris -> uris.addAll(redirectUris) }
            .scopes { scopes -> scopes.addAll(clientScopes) }

        val clientSettingsMap = parseMap(client.clientSettings)
        builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build())

        val tokenSettingsMap = parseMap(client.tokenSettings)
        builder.tokenSettings(TokenSettings.withSettings(tokenSettingsMap).build())

        return builder.build()
    }

    private fun toEntity(registeredClient: RegisteredClient): Client {
        val clientAuthenticationMethods = ArrayList<String>(registeredClient.clientAuthenticationMethods.size)
        registeredClient.clientAuthenticationMethods.forEach { clientAuthenticationMethod ->
            clientAuthenticationMethods.add(clientAuthenticationMethod.value)
        }
        val authorizationGrantTypes = ArrayList<String>(registeredClient.authorizationGrantTypes.size)
        registeredClient.authorizationGrantTypes.forEach { authorizationGrantType ->
            authorizationGrantTypes.add(authorizationGrantType.value)
        }
        return Client(
            id = registeredClient.id,
            clientId = registeredClient.clientId,
            clientIdIssuedAt = registeredClient.clientIdIssuedAt,
            clientSecret = registeredClient.clientSecret,
            clientSecretExpiresAt = registeredClient.clientSecretExpiresAt,
            clientName = registeredClient.clientName,
            clientAuthenticationMethods = StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods),
            authorizationGrantTypes = StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes),
            redirectUris = StringUtils.collectionToCommaDelimitedString(registeredClient.redirectUris),
            scopes = StringUtils.collectionToCommaDelimitedString(registeredClient.scopes),
            clientSettings = writeMap(registeredClient.clientSettings.settings),
            tokenSettings = writeMap(registeredClient.tokenSettings.settings)
        )
    }

    private fun parseMap(data: String): Map<String, Any> {
        return try {
            objectMapper.readValue(data, object: TypeReference<Map<String, Any>>() {})
        } catch (ex: Exception) {
            throw IllegalArgumentException(ex.message, ex)
        }
    }

    private fun writeMap(data: Map<String, Any>): String {
        return try {
            objectMapper.writeValueAsString(data)
        } catch (ex: Exception) {
            throw java.lang.IllegalArgumentException(ex.message, ex)
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

private fun resolveClientAuthenticationMethod(clientAuthenticationMethod: String): ClientAuthenticationMethod {
    return if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.value == clientAuthenticationMethod) {
        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
    } else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.value == clientAuthenticationMethod) {
        ClientAuthenticationMethod.CLIENT_SECRET_POST
    } else if (ClientAuthenticationMethod.NONE.value == clientAuthenticationMethod) {
        ClientAuthenticationMethod.NONE
    } else {
        ClientAuthenticationMethod(clientAuthenticationMethod)
    }
}