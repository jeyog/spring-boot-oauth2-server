package com.jeyog.oauth2.repository

import ch.qos.logback.core.net.server.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository: JpaRepository<Client, String> {
    fun findByClientId(clientId: String): Client?
}