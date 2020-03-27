package com.guangzhida.xiaomai.model

class UserModel {
    var data: Data? = null
    var message: Any? = ""
    var rel: Boolean = false
    var status = 0

    inner class Data(
        val age: Int,
        val description: Any,
        val headUrl: String,
        val id: String,
        val name: String,
        val password: String,
        val schoolName: Any,
        val sex: Int,
        val token: String,
        val username: String
    )
}


