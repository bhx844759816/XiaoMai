package com.guangzhida.xiaomai.ui.chat.adapter

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * 服务的多类型列表
 * @param status 0.问题列表  1.用户发送的问题  2.客服回复的答案 3.转人工
 */
class ServiceMultipleItem(val status: Int, val data: Any?) : MultiItemEntity {
    override val itemType: Int
        get() = status

    companion object {
        const val TYPE_SERVICE_PROBLEM_LIST = 0
        const val TYPE_USER_SEND_PROBLEM = 1
        const val TYPE_SERVICE_REPLY_PROBLEM = 2
        const val TYPE_PEOPLE_SERVICE = 3
    }
}