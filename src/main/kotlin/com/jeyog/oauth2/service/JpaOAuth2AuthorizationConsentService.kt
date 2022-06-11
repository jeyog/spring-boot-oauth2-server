package com.jeyog.oauth2.service

import com.jeyog.oauth2.entity.AuthorizationConsent
import com.jeyog.oauth2.repository.AuthorizationConsentRepository
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import org.springframework.util.StringUtils


@Component
class JpaOAuth2AuthorizationConsentService(
    private val authorizationConsentRepository: AuthorizationConsentRepository,
    private val registeredClientRepository: RegisteredClientRepository
): OAuth2AuthorizationConsentService {
    override fun save(authorizationConsent: OAuth2AuthorizationConsent?) {
        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null")
        this.authorizationConsentRepository.save(toEntity(authorizationConsent!!))
    }

    override fun remove(authorizationConsent: OAuth2AuthorizationConsent?) {
        Assert.notNull(authorizationConsent, "authorizationConsent cannot be null")
        this.authorizationConsentRepository.deleteByRegisteredClientIdAndPrincipalName(
            authorizationConsent!!.registeredClientId, authorizationConsent.principalName
        )
    }

    override fun findById(registeredClientId: String?, principalName: String?): OAuth2AuthorizationConsent? {
        Assert.hasText(registeredClientId, "registeredClientId cannot be empty")
        Assert.hasText(principalName, "principalName cannot be empty")
        return this.authorizationConsentRepository.findByRegisteredClientIdAndPrincipalName(registeredClientId!!, principalName!!)?.let { toObject(it) }
    }

    private fun toObject(authorizationConsent: AuthorizationConsent): OAuth2AuthorizationConsent {
        val registeredClientId = authorizationConsent.registeredClientId
        this.registeredClientRepository.findById(registeredClientId)
            ?: throw DataRetrievalFailureException(
                "The RegisteredClient with id '${registeredClientId}' was not found in the RegisteredClientRepository."
            )

        val builder = OAuth2AuthorizationConsent.withId(
            registeredClientId,
            authorizationConsent.principalName
        )
        if (authorizationConsent.authorities != null) {
            for (authority in StringUtils.commaDelimitedListToSet(authorizationConsent.authorities)) {
                builder.authority(SimpleGrantedAuthority(authority))
            }
        }

        return builder.build()
    }

    private fun toEntity(authorizationConsent: OAuth2AuthorizationConsent): AuthorizationConsent {
        val entity = AuthorizationConsent(
            registeredClientId = authorizationConsent.registeredClientId,
            principalName = authorizationConsent.principalName
        )

        val authorities = HashSet<String>()
        for (authority in authorizationConsent.authorities) {
            authorities.add(authority.authority)
        }
        entity.authorities = StringUtils.collectionToCommaDelimitedString(authorities)

        return entity
    }
}