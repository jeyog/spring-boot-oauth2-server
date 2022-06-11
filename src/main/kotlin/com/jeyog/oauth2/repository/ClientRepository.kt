package com.jeyog.oauth2.repository

import com.jeyog.oauth2.entity.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository: JpaRepository<Client, String> {
    fun findByClientId(clientId: String): Client?
}