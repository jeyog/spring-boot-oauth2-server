package com.jeyog.oauth2.entity

import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

@Entity
@IdClass(AuthorizationConsent.AuthorizationConsentId::class)
class AuthorizationConsent(
        @Id
        val registeredClientId: String,
        @Id
        val principalName: String,
        @Column(length = 1000)
        val authorities: String
) {
    class AuthorizationConsentId(
            private val registeredClientId: String,
            private val principalName: String
    ): Serializable {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as AuthorizationConsentId
            return registeredClientId == that.registeredClientId && principalName == that.principalName
        }

        override fun hashCode(): Int {
            return Objects.hash(registeredClientId, principalName)
        }
    }
}