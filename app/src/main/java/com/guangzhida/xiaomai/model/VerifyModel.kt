package com.guangzhida.xiaomai.model

data class VerifyModel(
    val code: Int,
    val data: Data,
    val msg: String
)

data class Data(
    val attrNum: Int,
    val attributes: List<Any>,
    val authenticator: Any,
    val basIp: Any,
    val challenge: Any,
    val chapPassword: Any,
    val errCode: Int,
    val errMessage: String,
    val isChap: Int,
    val packetTypeName: String,
    val password: Any,
    val reqId: Int,
    val rsv: Int,
    val secret: Any,
    val serialNo: Int,
    val textInfo: Any,
    val type: Int,
    val userIp: String,
    val userPort: Int,
    val username: Any,
    val ver: Int
)