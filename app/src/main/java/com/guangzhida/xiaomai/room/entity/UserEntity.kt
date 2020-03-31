package com.guangzhida.xiaomai.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey
    val uid: Long,
    val nickName: String,
    val userName: String,
    val avatarUrl: String
)