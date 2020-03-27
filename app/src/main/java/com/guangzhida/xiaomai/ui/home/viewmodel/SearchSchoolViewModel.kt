package com.guangzhida.xiaomai.ui.home.viewmodel

import androidx.lifecycle.MutableLiveData
import com.guangzhida.xiaomai.base.BaseViewModel
import com.guangzhida.xiaomai.data.InjectorUtil
import com.guangzhida.xiaomai.model.SchoolModel

/**
 * 搜索学校的ViewModel
 */
class SearchSchoolViewModel : BaseViewModel() {
    private val homeRepository = InjectorUtil.getHomeRepository()
    val mSchoolInfoLiveData = MutableLiveData<List<SchoolModel>>()

    fun getSchoolInfo() {
        launchGo({
            val schoolModelWrap = homeRepository.getSchoolInfo()
            if (schoolModelWrap.status == 200) {
                //请求成功
                mSchoolInfoLiveData.postValue(schoolModelWrap.result)
            }
        }, isShowDialog = false)
    }
}