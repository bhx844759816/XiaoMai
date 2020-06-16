package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.model.UserModel
import com.guangzhida.xiaomai.utils.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppointmentDetailsViewModel : BaseViewModel() {
    private var mSchoolSelectInfoGson by Preference(Preference.SCHOOL_SELECT_INFO_GSON, "")
    private val mSchoolModel by lazy {
        Gson().fromJson<SchoolModel>(mSchoolSelectInfoGson, SchoolModel::class.java)
    }
    private val mChatRepository = InjectorUtil.getChatRepository()
    private val mUserRepository = InjectorUtil.getUserRepository()

    val mSingUpResultObserver = MutableLiveData<Boolean>()
    val mUserInfoObserver = MutableLiveData<UserModel.Data>()

    fun getUserInfo(userId: String) {
        launchUI {
            try {
                val result = withContext(Dispatchers.IO) {
                    mUserRepository.getUserInfoByUserId(userId)
                }
                if (result.status == 200) {
                    mUserInfoObserver.postValue(result.data)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    /**
     * 报名参加活动
     */
    fun singUpActivity(aboutId: String) {
        launchUI {
            try {
                defUI.showDialog.call()
                val result = withContext(Dispatchers.IO) {
                    mChatRepository.signUpActivity(
                        mSchoolModel.id,
                        BaseApplication.instance().mUserModel!!.id,
                        aboutId,
                        "0"
                    )
                }
                if (result.isSuccess()) {
                    defUI.toastEvent.postValue("报名成功")
                    mSingUpResultObserver.postValue(true)
                } else {
                    defUI.toastEvent.postValue(result.message)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                defUI.toastEvent.postValue("网络错误请稍后重试")
            } finally {
                defUI.dismissDialog.call()
            }
        }
    }


}