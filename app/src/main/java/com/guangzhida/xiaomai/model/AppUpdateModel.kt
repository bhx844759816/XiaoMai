package com.guangzhida.xiaomai.model

//class AppUpdateModel {
//    var result: Data? = null
//    var msg = ""
//    var rel: Boolean = false
//    var status: Int = 0
//    {"msg":null,"result":{
//        "version":"v101","type":2,"url":"http://www.google.cn","appId":null,
//        "message":"增加聊天发送语音功能","appSize":null,"isForcibly":true},"count":0,"status":200}
//    inner class Data(
//        val appSize: String,
//        val type: Int,
//        val url: String,
//        val version: String,
//        val message: String,
//        val appId: String,
//        val isForcibly: Boolean //是否强制更新
//    )
////    "version": "v101",
////    "type": 2,
////    "url": "http://www.google.cn",
////    "appId": null,
////    "message": "增加聊天发送语音功能",
////    "appSize": null,
////    "isForcibly": true
//
//}

data class AppUpdateModel(
    val appSize: Long,
    val type: Int,
    val url: String = "",
    val version: Int,
    val message: String = "",
    val appId: String,
    val isForcibly: Boolean //是否强制更新
)