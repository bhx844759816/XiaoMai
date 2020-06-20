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
        const val TYPE_SERVICE_HELP_LIST = 0 //帮助列表
        const val TYPE_SERVICE_PROBLEM_LIST = 1 //问题列表
        const val TYPE_USER_SEND_PROBLEM = 2 //用户发送的问题
        const val TYPE_SERVICE_REPLY_PROBLEM = 3 //客服针对问题进行回复
        const val TYPE_PEOPLE_SERVICE = 4 //连接人工
        const val TYPE_SERVICE_SEND = 5 //客服主动发送的消息
    }
}