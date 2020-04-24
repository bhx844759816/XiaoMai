package com.guangzhida.xiaomai.model

/**
 * 问题当前的状态
 * @param status 0 初始状态 -1 未解决 1解决
 *
 * */
data class ProblemStatusModel(val problemModel: ServiceProblemModel, var status: Int)