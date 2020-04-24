package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.SERVICE_USERNAME
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.ServiceProblemModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.utils.Preference
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 客服的ViewModel
 */
class ServiceViewModel : BaseViewModel() {
    val mServiceProblemListModel = MutableLiveData<List<ServiceProblemModel>>()
    private val chatRepository = InjectorUtil.getChatRepository()

    /**
     *获取客服问题列表
     */
    fun getServiceProblemList() {
        launchUI {
            try {
                val result = withContext(Dispatchers.IO) {
                    chatRepository.getServiceProblemList()
                }
                if (result.isSuccess()) {
                    mServiceProblemListModel.postValue(result.result)
                }
            }catch (e:Throwable){
                e.printStackTrace()
            }

        }
    }

}