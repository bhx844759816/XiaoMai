package com.guangzhida.xiaomai.event

object LiveDataBusKey {

    const val NETWORK_CHANGE_KEY = "net_work_change" //网络状态改变
    const val MESSAGE_COUNT_CHANGE_KEY = "message_count_change" //消息个数改变

    const val ADD_FRIEND_RESULT_KEY = "add_friend_result"//添加好友的结果


    const val IM_CONNECT_SERVER_KEY = "im_connect_server_key"//连接到环信服务器
    const val IM_DISCONNECT_SERVER_KEY = "im_disconnect_server_key"//与服务器断开连接
    const val IM_KICKED_BY_OTHER_DEVICE = "im_kicked_by_other_device"//异地登录被踢下线


    const val LOGIN_KEY = "login"
    const val SCHOOL_MODEL_CHANGE_KEY = "school_model_change" //选择学校改变
    const val PUBLISH_APPOINTMENT_FINISH_KEY = "publish_appointment_finish_key"//发布约吗完成 通知列表刷新
}