package com.guangzhida.xiaomai.base

data class BaseResult<T>(
    val message: String,
    val status: Int,
    val result: T
) : IBaseResponse<T> {
    override fun code(): Int = status

    override fun msg(): String = message

    override fun data(): T = result

    override fun isSuccess(): Boolean = status == 200
}