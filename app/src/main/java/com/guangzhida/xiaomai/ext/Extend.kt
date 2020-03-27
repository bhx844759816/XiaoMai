package com.guangzhida.xiaomai.ext

import com.guangzhida.xiaomai.utils.Base64Utils
import com.guangzhida.xiaomai.utils.RSAUtil

/**
 * 针对字符串加密
 */
fun String.rsAEncode(): String {
    val keyPair = RSAUtil.generateRSAKeyPair(1024)
    // 获取公钥和私钥
    val aPublic = keyPair.public
    val aPublicEncoded = aPublic.encoded
    try { // 公钥加密
        val bytes =
            RSAUtil.encryptByPublicKey("java", this.toByteArray(), aPublicEncoded)
        return Base64Utils.encode(bytes)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

/**
 * 针对字符串解密
 */
fun String.rsADecode(): String {
    val keyPair = RSAUtil.generateRSAKeyPair(1024)
    val aPrivate = keyPair.private
    val aPrivateEncoded = aPrivate.encoded
    try { // 公钥加密
        val encodeBytes = Base64Utils.decode(this)
        // 私钥解密
        val bytes1 =
            RSAUtil.decryptByPrivateKey("java", encodeBytes, aPrivateEncoded)
        return String(bytes1)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}