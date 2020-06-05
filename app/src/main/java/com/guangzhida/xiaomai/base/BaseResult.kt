package com.guangzhida.xiaomai.base

data class BaseResult<T>(
    val message: String,
    val status: Int,
    val data: T
) : IBaseResponse<T> {
    override fun code(): Int = status

    override fun msg(): String = message

    override fun data(): T = data

    override fun isSuccess(): Boolean = status == 200
}