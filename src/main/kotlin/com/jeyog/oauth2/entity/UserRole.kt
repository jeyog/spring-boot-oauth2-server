package com.jeyog.oauth2.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonValue
import org.springframework.security.core.GrantedAuthority

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class UserRole: GrantedAuthority {
    ROLE_USER,
    ROLE_ADMIN;

    @JsonValue
    override fun getAuthority(): String {
        return name
    }
}