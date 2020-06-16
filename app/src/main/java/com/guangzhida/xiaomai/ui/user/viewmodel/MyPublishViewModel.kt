package com.guangzhida.xiaomai.ui.user.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.SchoolModel
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
    val mDeleteItemObserver = MutableLiveData<AppointmentModel>()

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
                    mPublishListErrorObserver.postValue(true)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                defUI.toastEvent.postValue("网络错误")
                mPublishListErrorObserver.postValue(true)
            }
        }
    }


    fun deleteMyPublish(list: MutableList<AppointmentModel>) {
        launchUI {
            try {
                defUI.showDialog.call()
                list.forEach {
                    val result = withContext(Dispatchers.IO) {
                        mRepository.deleteMyPublishAppointment(it.id.toString())
                    }
                    if (result.isSuccess()) {
                        mDeleteItemObserver.postValue(it)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                defUI.dismissDialog.call()
            }
        }
    }

}