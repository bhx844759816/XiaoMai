package com.guangzhida.xiaomai.ui.home.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.model.PackageInfoModel

class AccountPackageAdapter(list: MutableList<PackageInfoModel>, callBack: ((PackageInfoModel) -> Unit)? = null) :
    BaseQuickAdapter<PackageInfoModel, BaseViewHolder>(
        R.layout.adapter_account_package_layout,
        list
    ) {
    private val mCallBack = callBack
    override fun convert(helper: BaseViewHolder, item: PackageInfoModel) {
        val tvTitle = helper.getView<TextView>(R.id.tvTitle)
        val tvMoney = helper.getView<TextView>(R.id.tvMoney)
        val tvBuy = helper.getView<TextView>(R.id.tvBuy)
        val ivCheck = helper.getView<ImageView>(R.id.ivCheck)
        tvTitle.text = item.servername ?: ""
        tvMoney.text = buildString {
            append("￥")
            append(item.price ?: "")
        }
        if (item.current_use != null && "1" == item.current_use) {
            ivCheck.visibility = View.VISIBLE
        } else {
            ivCheck.visibility = View.GONE
        }
        tvBuy.setOnClickListener {
            mCallBack?.invoke(item)
        }
    }
}