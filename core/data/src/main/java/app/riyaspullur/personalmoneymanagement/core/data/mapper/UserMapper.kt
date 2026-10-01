package app.riyaspullur.personalmoneymanagement.core.data.mapper

import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.User

fun UserEntity.toDomain() = User(
    id = id,
    username = username,
    passwordHash = passwordHash,
    displayName = displayName,
    createdAt = createdAt
)

fun User.toEntity() = UserEntity(
    id = id,
    username = username,
    passwordHash = passwordHash,
    displayName = displayName,
    createdAt = createdAt
)
