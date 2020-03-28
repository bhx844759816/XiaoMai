package com.guangzhida.xiaomai.model

data class SchoolInfoModel(
    val status: Int,
    val message: String,
    val rel: Boolean,
    val data: SchoolModel
)