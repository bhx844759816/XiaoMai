package com.guangzhida.xiaomai.model

data class SchoolModelWrap(
    val status: Int,
    val msg: String,
    val count: Int,
    val data: List<SchoolModel>
)