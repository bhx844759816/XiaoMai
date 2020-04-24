package com.guangzhida.xiaomai.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userName: String = "",//用户id
    val avatarUrl: String = "", //头像
    val nickName: String = "", //昵称
    val remarkName: String = "", //备注姓名
    val sex: String = "",
    val age: String = "",
    var isTop: Boolean = false, //是否置顶
    val lastMessageTime: Long =0, //最后条消息得时间用于排序
    val parentUserName: String = ""//所属那个用户的会话
)

//userName ->用户id
//用户头像
//用户昵称
//用户性别
//是否置顶
//lastMessageTime 最后条消息得时间用于排序

