package com.guangzhida.xiaomai.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户会话的对象
 */
@Entity
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var userName: String = "",//用户id
    var avatarUrl: String = "", //头像
    var nickName: String = "", //昵称
    var remarkName: String = "", //备注姓名
    var sex: String = "",
    var age: String = "",
    var isTop: Boolean = false, //是否置顶
    var lastMessageTime: Long = 0, //最后条消息得时间用于排序
    var parentUserName: String = "",//所属那个用户的会话
    var type: Int = 0 // type 0 是默认会话 1是客服会话 2陌生人会话
)


