package com.jeyog.oauth2.repository

import com.jeyog.oauth2.entity.Authorization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AuthorizationRepository: JpaRepository<Authorization, String> {
    fun findByState(state: String): Authorization?
    fun findByAuthorizationCodeValue(authorizationCode: String): Authorization?
    fun findByAccessTokenValue(accessToken: String): Authorization?
    fun findByRefreshTokenValue(refreshToken: String): Authorization?
    @Query("select a from Authorization a where a.state = :token" +
            " or a.authorizationCodeValue = :token" +
            " or a.accessTokenValue = :token" +
            " or a.refreshTokenValue = :token"
    )
    fun findByStateOrAuthorizationCodeValueOrAccessTokenValueOrRefreshTokenValue(token: String): Authorization?
}