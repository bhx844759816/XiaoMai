package com.guangzhida.xiaomai.ui.home

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.SEARCH_SCHOOL_KEY
import com.guangzhida.xiaomai.SEARCH_SCHOOL_USER_REGISTER_KEY
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.ui.home.viewmodel.SearchSchoolViewModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.SPUtils
import kotlinx.android.synthetic.main.activity_search_school.*

/**
 * 搜索学校界面
 */
class SearchSchoolActivity : BaseActivity<SearchSchoolViewModel>() {
    private var mSchoolModelList: List<SchoolModel>? = null
    override fun layoutId(): Int = R.layout.activity_search_school

    override fun initView(savedInstanceState: Bundle?) {
        mViewModel.getSchoolInfo()
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        mViewModel.mSchoolInfoLiveData.observe(this, Observer {
            mSchoolModelList = it
            it.forEachIndexed { index, schoolModel ->
                val chip = Chip(this@SearchSchoolActivity)
                chip.id = index
                chip.text = schoolModel.name
                chip.setTextColor(resources.getColor(R.color.defaultTextColor))
                chip.chipStrokeColor =
                    ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
                chip.chipBackgroundColor = ColorStateList.valueOf(resources.getColor(R.color.white))
                chip.chipStrokeWidth = 1f
                chip.setOnClickListener {

                    //注册的url
                    SPUtils.put(
                        this,
                        SEARCH_SCHOOL_USER_REGISTER_KEY,
                        schoolModel.regiestNewUser
                    )
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                chipGroup.addView(chip)
            }

        })
    }
}