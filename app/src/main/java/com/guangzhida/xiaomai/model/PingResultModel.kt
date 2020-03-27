package com.guangzhida.xiaomai.model

data class PingResultModel(
    var success: Boolean = false,
    var lossPackageRate: String = "100%",
    var averageDelay: String = "0.0ms"
)