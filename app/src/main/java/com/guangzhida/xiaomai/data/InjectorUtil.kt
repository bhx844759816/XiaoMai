package com.guangzhida.xiaomai.data

import com.guangzhida.xiaomai.data.chat.ChatNetWork
import com.guangzhida.xiaomai.data.chat.ChatRepository
import com.guangzhida.xiaomai.data.home.HomeNetWork
import com.guangzhida.xiaomai.data.home.HomeRepository
import com.guangzhida.xiaomai.data.login.LoginNetwork
import com.guangzhida.xiaomai.data.login.LoginRepository

object InjectorUtil {
    fun getHomeRepository() = HomeRepository.getInstance(HomeNetWork.getInstance())

    fun getChatRepository() = ChatRepository.getInstance(ChatNetWork.getInstance())

    fun getLoginRepository() = LoginRepository.getInstance(LoginNetwork.getInstance())
}