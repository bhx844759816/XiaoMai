package com.guangzhida.xiaomai.model

class ModifyUserModel {
    var data: Data? = null
    var message: String? = ""
    var rel: Boolean = false
    var status = 0

    inner class Data(
        val nickName: String,
        val headId: String,
        val headUrl: String,
        val sex: Int,
        val age: Int,
        val schoolName: String,
        val signature: String?
    )
}

