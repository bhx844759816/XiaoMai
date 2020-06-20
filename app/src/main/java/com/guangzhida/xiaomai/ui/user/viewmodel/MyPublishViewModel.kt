package com.guangzhida.xiaomai.ui.user.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentMultipleItem
import com.guangzhida.xiaomai.utils.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 我的发布
 */
class MyPublishViewModel : BaseViewModel() {
    private val mRepository = InjectorUtil.getUserRepository()
    private var mSchoolSelectInfoGson by Preference(Preference.SCHOOL_SELECT_INFO_GSON, "")
    private val mSchoolModel by lazy {
        Gson().fromJson<SchoolModel>(mSchoolSelectInfoGson, SchoolModel::class.java)
    }

    val mPublishListObserver = MutableLiveData<List<AppointmentModel>>()
    val mPublishListErrorObserver = MutableLiveData<Boolean>()
    val mDeleteItemObserver = MutableLiveData<AppointmentMultipleItem>()
    val mDeleteItemFinishObserver = MutableLiveData<Boolean>()

    /**
     * 获取我发布的约吗
     */
    fun getList() {
        launchUI {
            try {
                val result = withContext(Dispatchers.IO) {
                    mRepository.getMyPublishAppointmentList(
                        BaseApplication.instance().mUserModel!!.id,
                        mSchoolModel.id
                    )
                }
                if (result.isSuccess()) {
                    mPublishListObserver.postValue(result.data)
                } else {
                    defUI.toastEvent.postValue(result.message)
                    mPublishListObserver.postValue(result.data)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                defUI.toastEvent.postValue("网络错误")
                mPublishListErrorObserver.postValue(true)
            }
        }
    }

    /**
     * 删除我的发布
     */
    fun deleteMyPublish(list: MutableList<AppointmentMultipleItem>) {
        launchUI {
            try {
                defUI.showDialog.call()
                list.forEach {
                    val result = withContext(Dispatchers.IO) {
                        mRepository.deleteMyPublishAppointment(it.item.id.toString())
                    }
                    if (result.isSuccess()) {
                        mDeleteItemObserver.postValue(it)
                    }
                }

            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                defUI.dismissDialog.call()
                mDeleteItemFinishObserver.postValue(true)
            }
        }
    }

}