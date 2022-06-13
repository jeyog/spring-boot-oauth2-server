package com.jeyog.oauth2.converter

import com.jeyog.oauth2.entity.UserRole
import javax.persistence.AttributeConverter
import javax.persistence.Convert

@Convert
class UserRoleArrayConverter: AttributeConverter<List<UserRole>, String> {
    private val delimiters = ","
    override fun convertToDatabaseColumn(attribute: List<UserRole>?): String {
        return attribute!!.joinToString(delimiters, transform = UserRole::getAuthority)
    }

    override fun convertToEntityAttribute(dbData: String?): List<UserRole> {
        return dbData!!.split(delimiters).map { UserRole.valueOf(it) }
    }
}