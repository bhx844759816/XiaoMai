package com.guangzhida.xiaomai.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivityForResult
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.PackageInfoModel
import com.guangzhida.xiaomai.ui.WebActivity
import com.guangzhida.xiaomai.ui.home.adapter.AccountPackageAdapter
import com.guangzhida.xiaomai.ui.home.viewmodel.AccountPackageModifyViewModel
import com.guangzhida.xiaomai.ui.home.viewmodel.HomeViewModel
import com.guangzhida.xiaomai.utils.DateUtils
import com.guangzhida.xiaomai.utils.LogUtils
import kotlinx.android.synthetic.main.activity_account_package_modify_layout.*

/**
 * 套餐修改
 */
class AccountPackageModifyActivity : BaseActivity<AccountPackageModifyViewModel>() {
    private var findSecret: String? = null
    private var mAccountModel: AccountModel? = null
    private var isModifyPackage = false
    private val mPackageList = mutableListOf<PackageInfoModel>()
    private val mAdapter by lazy {
        AccountPackageAdapter(mPackageList) {
            doBuyPackage(it)
        }
    }

    override fun layoutId(): Int = R.layout.activity_account_package_modify_layout

    override fun initView(savedInstanceState: Bundle?) {
        registerObserver()
        findSecret = intent.getStringExtra("findSecret")
        mAccountModel = intent.getSerializableExtra("AccountModel") as AccountModel?
        mAccountModel?.let {
            mViewModel.getPackageInfo(it.user)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
        toolBar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        if (isModifyPackage) {
            setResult(Activity.RESULT_OK)
        }
        super.onDestroy()
    }
    /**
     *
     */
    private fun registerObserver() {
        mViewModel.mPackageInfoObserver.observe(this, Observer {
            mPackageList.clear()
            mPackageList.addAll(it)
            mAdapter.notifyDataSetChanged()
        })
        //清空套餐成功
        mViewModel.mClearPackageObserver.observe(this, Observer {
            if (it) {
                modifyAccountPackage()
            }
        })
        //获取账号信息
        mViewModel.mAccountModelData.observe(this, Observer {
            val viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
            mAccountModel = it
            viewModel.mAccountModelData.postValue(mAccountModel)
        })
    }

    /**
     * 在线更换套餐
     */
    private fun doBuyPackage(model:PackageInfoModel) {
        if (mAccountModel?.servername.isNullOrEmpty() || (model.current_use != null && model.current_use == "1" )) {
            //执行购买
            modifyAccountPackage()
        } else {
            val timeSpace = DateUtils.getTwoDay(
                mAccountModel!!.expiretime,
                DateUtils.dateToStr(DateUtils.getNow())
            )
            if (timeSpace > 3) {
                showModifyAccountTipsDialog()
            } else {
                modifyAccountPackage()
            }
        }
    }

    /**
     * 跳转到修改套餐页面
     */
    private fun modifyAccountPackage() {
        startKtxActivityForResult<WebActivity>(
            requestCode = 0x01, values = listOf(
            Pair("url","http://yonghu.guangzhida.cn/lfradius/home.php?a=userlogin&c=login"),
//                Pair("url", "http://yonghu.guangzhida.cn/lfradius/home.php/user/server"),
                Pair("type", "AccountRecharge"),
                Pair("params", "username=${mAccountModel?.user}&password=${mAccountModel?.pass}")
            )
        )
        isModifyPackage = true
    }

    /**
     * 提示修改套餐的Dialog
     */
    private fun showModifyAccountTipsDialog() {
        MaterialDialog(this)
            .cancelable(true)
            .cornerRadius(8f)
            .title(text = "重要提醒")
            .message(text = "您的套餐剩余时长多余3天，如果您强制更换套餐，您的账户余额及剩余时长将会被清零，且无法找回。您确定要这样操作吗？")
            .lifecycleOwner(this)
            .positiveButton(text = "确定") { dialog ->
                dialog.dismiss()
                mAccountModel?.let {
                    mViewModel.clearAccountPackage(it.user, findSecret ?: "")
                }
            }.negativeButton(text = "取消") { dialog ->
                dialog.dismiss()
            }.show()
    }
}