package com.jeyog.oauth2.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.jeyog.oauth2.converter.UserRoleArrayConverter
import javax.persistence.Convert

abstract class UserMixIn @JsonCreator constructor(
    @JsonProperty id: Long,
    @JsonProperty username: String,
    @Convert(converter = UserRoleArrayConverter::class) @JsonProperty val roles: List<UserRole>
) {}