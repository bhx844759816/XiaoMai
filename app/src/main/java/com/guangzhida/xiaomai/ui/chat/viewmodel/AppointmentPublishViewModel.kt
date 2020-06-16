package com.guangzhida.xiaomai.ui.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.Preference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppointmentPublishViewModel : BaseViewModel() {
    private val mCommonRepository = InjectorUtil.getCommonRepository()
    private val mChatRepository = InjectorUtil.getChatRepository()
    private var mSchoolSelectInfoGson by Preference(Preference.SCHOOL_SELECT_INFO_GSON, "")
    private val mSchoolModel by lazy {
        Gson().fromJson<SchoolModel>(mSchoolSelectInfoGson, SchoolModel::class.java)
    }

    val mSubmitResultObserver = MutableLiveData<Boolean>()
    fun doSubmit(
        title: String,
        dec: String,
        address: String,
        moneyType: Int,
        money: String,
        activityTime: String,
        signUpEndTime: String,
        boyPeoples: String,
        girlPeoples: String,
        photos: List<String>
    ) {
        launchUI {
            try {
                defUI.showDialog.call()
                val params = mutableMapOf<String, Any>()
                val activityPic = withContext(Dispatchers.IO) {
                    val photoPics = mutableListOf<String>()
                    photos.forEach { filePath ->
                        if (File(filePath).exists()) {
                            val result = mCommonRepository.uploadImg(File(filePath))
                            if (result.status == 200) {
//                                val photoId = result.message.split(":")[0]
                                val photoUrl = result.message.split(":")[1]
                                photoPics.add(photoUrl)
                            }
                        }
                    }
                    LogUtils.i("上传图片 photoPics=${photoPics}")
                    if (photoPics.size > 1) {
                        photoPics.joinToString(separator = ",")
                    } else {
                        photoPics[0]
                    }
                }
                LogUtils.i("上传图片=${activityPic}")
                params["schoolId"] = mSchoolModel.id
                params["userId"] = BaseApplication.instance().mUserModel!!.id
                params["title"] = title
                params["content"] = dec
                params["signEndTime"] = signUpEndTime
                params["activityStartTime"] = activityTime
                params["activityAddress"] = address
                params["activityPic"] = activityPic
                params["activityMoney"] = money
                params["boyCount"] = boyPeoples
                params["girlCount"] = girlPeoples
                params["feeType"] = moneyType
                val submitResult = withContext(Dispatchers.IO) {
                    mChatRepository.submitAppointmentData(params)
                }
                if (submitResult.isSuccess()) {
                    mSubmitResultObserver.postValue(true)
                    defUI.toastEvent.postValue("发布成功")
                } else {
                    mSubmitResultObserver.postValue(false)
                    defUI.toastEvent.postValue(submitResult.message)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                mSubmitResultObserver.postValue(false)
                defUI.toastEvent.postValue("发布失败，网络错误")
            } finally {
                defUI.dismissDialog.call()
            }
        }
    }
}