package com.guangzhida.xiaomai.ui.user

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fengchen.uistatus.UiStatusController
import com.fengchen.uistatus.annotation.UiStatus
import com.fengchen.uistatus.listener.OnCompatRetryListener
import com.google.gson.Gson
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ui.appointment.AppointmentDetailsActivity
import com.guangzhida.xiaomai.ui.appointment.adapter.AppointmentMultipleItem
import com.guangzhida.xiaomai.ui.user.adapter.MySingUpAppointmentAdapter
import com.guangzhida.xiaomai.ui.user.viewmodel.MySignUpViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.view.RecyclerViewItemDivision
import kotlinx.android.synthetic.main.activity_my_sign_up_layout.*

class MySignUpActivity : BaseActivity<MySignUpViewModel>() {
    private val mList = mutableListOf<AppointmentMultipleItem>()
    private val mAdapter by lazy {
        MySingUpAppointmentAdapter(mList)
    }
    private var isEdit = false
    private val mGson by lazy {
        Gson()
    }
    private lateinit var mUiStatusController: UiStatusController


    override fun layoutId(): Int = R.layout.activity_my_sign_up_layout

    override fun initView(savedInstanceState: Bundle?) {
        mUiStatusController = UiStatusController.get().bind(flContent)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(RecyclerViewItemDivision(20, Color.parseColor("#f7f7f7")))
        recyclerView.adapter = mAdapter
        mViewModel.getList()
        mUiStatusController.onCompatRetryListener =
            OnCompatRetryListener { _, _, _, _ ->
                mViewModel.getList()
            }
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        rlEditList.clickN {
            changeEditStatus()
        }
        //全选取消全选
        cbSelect.setOnCheckedChangeListener { _, isChecked ->
            mList.forEach {
                it.item.isChecked = isChecked
            }
            cbSelect.text = if (isChecked) {
                "取消全选"
            } else {
                "全选"
            }
            mAdapter.notifyDataSetChanged()
        }
        mAdapter.mItemCheckCallBack = { item, isCheck ->
            mList.find {
                it.item.id == item.id
            }?.item?.isChecked = isCheck
        }
        mAdapter.mItemClickCallBack = {
            startKtxActivity<AppointmentDetailsActivity>(
                value = Pair(
                    "appointmentModel",
                    mGson.toJson(it)
                )
            )
        }
        rlDelete.clickN {
            val list = mList.filter {
                it.item.isChecked
            }.toMutableList()
            if (list.isEmpty()) {
                ToastUtils.toastShort("请选择需要删除的条目")
                return@clickN
            }
            mViewModel.deleteMySignUp(list)
        }
        mViewModel.mPublishListObserver.observe(this, Observer {
            if (it.isEmpty()) {
                mUiStatusController.changeUiStatus(UiStatus.EMPTY)
            } else {
                mUiStatusController.changeUiStatus(UiStatus.CONTENT)
                val list = it.map {
                    AppointmentMultipleItem(it)
                }
                mList.clear()
                mList.addAll(list)
                mAdapter.notifyDataSetChanged()
            }
        })
        mViewModel.mPublishListErrorObserver.observe(this, Observer {
            mUiStatusController.changeUiStatus(UiStatus.NETWORK_ERROR)
        })
        mViewModel.mDeleteItemObserver.observe(this, Observer {
            mList.remove(it)
            mAdapter.notifyDataSetChanged()
        })
        mViewModel.mDeleteItemFinishObserver.observe(this, Observer {
            changeEditStatus()
        })
    }

    private fun changeEditStatus() {
        isEdit = !isEdit
        if (isEdit) {
            tvEdit.text = "取消编辑"
        } else {
            tvEdit.text = "编辑"
        }
        llBottomControl.visibility = if (isEdit) View.VISIBLE else View.GONE
        mList.forEach {
            it.item.isEdit = isEdit
        }
        mAdapter.notifyDataSetChanged()
    }
}