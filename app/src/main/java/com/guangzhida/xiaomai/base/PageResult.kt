package com.guangzhida.xiaomai.base

data class PageResult<T>(
    val message: String,
    val status: Int,
    val data: Result<T>
)

data class Result<T>(
    val total: Int,
    val rows: List<T>
)