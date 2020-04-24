package com.guangzhida.xiaomai.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 存储的好友列表
 */
@Entity
data class UserEntity(
    @PrimaryKey
    var uid: Long,
    var nickName: String = "", //昵称
    var userName: String = "", //手机号
    var avatarUrl: String = "", //头像
    var age: String = "", //年龄
    var sex: String = "",//性别
    var remarkName: String = "",//备注信息
    var singUp: String = ""//个性签名
)


