package com.guangzhida.xiaomai.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 好友邀请对象
 */
@Entity
data class InviteMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var nickName: String? = null,
    var headerUrl: String? = null,
    val from: String, //发送过来的好友名称
    val time: Long, //接收到消息得时间
    val reason: String,//添加好友的原因
    val userName: String, //当前用户的名称
    var state: Int //0初始状态 1待验证我发送过去的  2同意好友  3拒绝好友
)

