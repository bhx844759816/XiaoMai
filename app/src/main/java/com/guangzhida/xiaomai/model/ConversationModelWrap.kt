package com.guangzhida.xiaomai.model

import com.guangzhida.xiaomai.room.entity.ConversationEntity
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.hyphenate.chat.EMConversation

data class ConversationModelWrap(
    val emConversation: EMConversation?,
    val conversationEntity: ConversationEntity
)