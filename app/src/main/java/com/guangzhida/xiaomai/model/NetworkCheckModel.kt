package com.guangzhida.xiaomai.model

import java.util.stream.Stream

data class NetworkCheckModel(
    val content: String, //检测结果
    val checkSuccess: Boolean //网络状态是否正确
)