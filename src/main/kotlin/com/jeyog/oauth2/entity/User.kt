package com.jeyog.oauth2.entity

import com.jeyog.oauth2.converter.UserRoleArrayConverter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.*

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    private val username: String,
    private var password: String,

    @Convert(converter = UserRoleArrayConverter::class)
    var roles: MutableCollection<UserRole>
): UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return roles.toMutableList()
    }

    fun setAuthorities(vararg roles: UserRole) {
        this.roles.addAll(roles)
    }

    override fun getPassword(): String {
        return password
    }

    fun setPassword(password: String) {
        this.password = password
    }

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}