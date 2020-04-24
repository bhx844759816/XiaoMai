package com.guangzhida.xiaomai.model

import com.guangzhida.xiaomai.room.entity.UserEntity

data class ChatMessageRecordModel(
    val messageId: String,
    val message: String,
    val atTime: Long,
    val userEntity: UserEntity
)