package com.guangzhida.xiaomai.ui.appointment.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.SchoolModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 约吗
 */
class AppointmentViewModel : BaseViewModel() {
    private var pageSize = 10
    private var pageNum = 1
    private val mRepository = InjectorUtil.getChatRepository()
    private val mHomeRepository = InjectorUtil.getHomeRepository()
    val mRefreshObserver = MutableLiveData<Boolean>() //刷新结果的回调
    val mResultListObserver = MutableLiveData<Pair<Boolean, List<AppointmentModel>>>()
    val mSchoolModelListData = MutableLiveData<List<SchoolModel>>()
    var mType = ""
    /**
     * 获取数据分页
     */
    fun getData(isRefresh: Boolean, schoolId: String, type: String) {
        launchUI {
            try {
                if (isRefresh) {
                    pageNum = 1
                }
                if (mType != type) {
                    pageNum = 1
                    mType = type
                }
                val result = withContext(Dispatchers.IO) {
                    mRepository.getAppointmentData(
                        schoolId,
                        BaseApplication.instance().mUserModel!!.id,
                        type,
                        pageSize.toString(), pageNum.toString()
                    )
                }
                if (result.status == 200) {
                    if (result.data.rows.isNotEmpty()) {
                        pageNum++
                    }
                    mResultListObserver.postValue(Pair(isRefresh, result.data.rows))
                    mRefreshObserver.postValue(true)
                } else {
                    mRefreshObserver.postValue(false)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                mRefreshObserver.postValue(false)
            }
        }
    }

    /**
     * 获取全部学校信息
     */
    fun getAllSchoolInfo() {
        launchGo(
            {
                val schoolModelWrap = mHomeRepository.getSchoolInfo()
                if (schoolModelWrap.status == 200) {
                    mSchoolModelListData.postValue(schoolModelWrap.data)
                }
            }, isShowDialog = false
        )
    }


    /**
     * 报名参加活动
     */
    fun singUpActivity(aboutId: String, schoolId: String) {
        launchUI {
            try {
                defUI.showDialog.call()
                val result = withContext(Dispatchers.IO) {
                    mRepository.signUpActivity(
                        schoolId,
                        BaseApplication.instance().mUserModel!!.id,
                        aboutId,
                        "0"
                    )
                }
                if (result.isSuccess()) {
                    defUI.toastEvent.postValue("报名成功")
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