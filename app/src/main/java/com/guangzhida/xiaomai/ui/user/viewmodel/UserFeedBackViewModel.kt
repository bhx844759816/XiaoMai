package com.guangzhida.xiaomai.ui.user.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.ktxlibrary.ext.logd
import com.guangzhida.xiaomai.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.ArrayList

/**
 * 用户反馈
 */
class UserFeedBackViewModel : BaseViewModel() {
    private val mUserRepository = InjectorUtil.getUserRepository()
    val mSubmitResultObserver = MutableLiveData<Boolean>()

    fun submitFeedBackData(content: String, phone: String, mPhotoList: ArrayList<String>) {
        launchUI {
            try {
                defUI.showDialog.call()
                val params = mutableMapOf<String, String>()
                params["userId"] = BaseApplication.instance().mUserModel?.id ?: ""
                params["problem"] = content
                params["mobilePhone"] = phone
                if (mPhotoList.isNotEmpty()) {
                    val mPhotoParams = StringBuilder()
                    mPhotoList.forEach { photoPath ->
                        val result = withContext(Dispatchers.IO) {
                            mUserRepository.uploadImg(File(photoPath))
                        }
                        if (result.status == 200) {
                            val photoId = result.message.split(":")[0]
                            val photoUrl = result.message.split(":")[1]
                            mPhotoParams.append(photoUrl)
                            mPhotoParams.append(",")
                        }
                    }
                    val mPhotoParamsStr =
                        mPhotoParams.substring(0, mPhotoParams.length - 1).toString()
                    LogUtils.i("mPhotoParamsStr=$mPhotoParamsStr")
                    params["pictures"] = mPhotoParamsStr
                }
                val result = withContext(Dispatchers.IO) {
                    mUserRepository.uploadUserFeedBack(params)
                }
                if (result.isSuccess()) {
                    defUI.toastEvent.postValue("感谢您的提交，客服会尽快处理您的问题！")
                    withContext(Dispatchers.IO) {
                        delay(800)
                        mSubmitResultObserver.postValue(true)
                    }
                } else {
                    defUI.toastEvent.postValue(result.message)
                    mSubmitResultObserver.postValue(false)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                defUI.toastEvent.postValue("提交失败请稍后再试")
                mSubmitResultObserver.postValue(false)
            } finally {
                defUI.dismissDialog.call()
            }
        }
    }
}