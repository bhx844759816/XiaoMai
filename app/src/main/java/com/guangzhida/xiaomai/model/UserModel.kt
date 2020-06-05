package com.guangzhida.xiaomai.model

class UserModel {
    var data: Data? = null
    var message: String? = ""
    var rel: Boolean = false
    var status = 0
    inner class Data(
        var age: Int,
        var description: Any,
        var headUrl: String,
        var id: String,
        var name: String,
        var password: String,
        var schoolName: Any,
        var sex: Int,
        var token: String,
        var username: String,
        var signature: String?,
        var campusNetworkNum: String?,
        var campusNetworkPwd: String?
    ) {
        override fun toString(): String {
            return "Data(age=$age, description=$description, headUrl='$headUrl', id='$id', " +
                    "name='$name', password='$password', schoolName=$schoolName, sex=$sex, token='$token', " +
                    "username='$username', signature='$signature',campusNetworkNum='$campusNetworkNum',campusNetworkPwd='$campusNetworkPwd')"
        }
    }
}


