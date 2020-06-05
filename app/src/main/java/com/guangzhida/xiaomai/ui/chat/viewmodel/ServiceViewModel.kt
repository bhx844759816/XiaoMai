package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.ServiceModel
import com.guangzhida.xiaomai.model.ServiceProblemModel
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 客服的ViewModel
 */
class ServiceViewModel : BaseViewModel() {
    val mServiceProblemListModel = MutableLiveData<List<ServiceProblemModel>>()

    val mServiceResult = MutableLiveData<ServiceModel>()
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
                    mServiceProblemListModel.postValue(result.data)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 查询服务器在线客服 进行客服沟通
     */
    fun searchOnlineService(schoolId: String) {
        launchUI {
            try {
                defUI.showDialog.call()
                val result = withContext(Dispatchers.IO) {
                    chatRepository.getOnlineServer(schoolId)
                }
                if (result.isSuccess()) {
                    mServiceResult.postValue(result.data)
                } else {
                    defUI.toastEvent.postValue(result.message)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                defUI.dismissDialog.call()
            }

        }
    }

}