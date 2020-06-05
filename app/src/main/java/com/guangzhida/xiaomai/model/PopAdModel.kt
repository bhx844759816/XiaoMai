package com.guangzhida.xiaomai.model

/**
 * 弹窗广告的对象
 */
data class PopAdModel(
    val id: String,
    val schoolId: String,
    val title: String?,
    val content: String?,
    val type: Int, // 1.纯文本 2.内部跳转 3.图片链接
    val link: String?,
    val gif: String?,
    val isShow: Int
)
