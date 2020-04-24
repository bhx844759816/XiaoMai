package com.guangzhida.xiaomai.model

import com.guangzhida.xiaomai.room.entity.UserEntity

data class SearchMessageModel(
    val userEntity: UserEntity,//检索到好友的记录
    var messageCount: Long //检索到聊天记录的条数
)