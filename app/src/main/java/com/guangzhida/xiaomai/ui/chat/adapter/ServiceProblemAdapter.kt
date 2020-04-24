package com.guangzhida.xiaomai.ui.chat.adapter

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.model.ProblemStatusModel
import com.guangzhida.xiaomai.model.ServiceProblemModel
import com.guangzhida.xiaomai.view.chat.SimpleCommonUtils
import com.hyphenate.chat.EMMessage

/**
 * 服务问题适配器
 */
class ServiceProblemAdapter(data: MutableList<ServiceMultipleItem>) :
    BaseMultiItemQuickAdapter<ServiceMultipleItem, BaseViewHolder>(data = data) {

    var mConnectPeopleServiceCallBack: (() -> Unit)? = null //连接人工服务
    var mClickProblemItemCallBack: ((ServiceProblemModel) -> Unit)? = null//点击问题Item的回调
    var mClickAnswerItemCallBack: ((ProblemStatusModel) -> Unit)? = null//点击已解决 未解决的回调
    var mAvatarUrl: String? = BaseApplication.instance().mServiceModel?.headUrl ?: ""

    init {
        addItemType(
            ServiceMultipleItem.TYPE_SERVICE_PROBLEM_LIST,
            R.layout.adapter_service_type_problem_list_layout
        )//问题列表
        addItemType(
            ServiceMultipleItem.TYPE_USER_SEND_PROBLEM,
            R.layout.adapter_service_type_user_send_layout
        )//用户发送
        addItemType(
            ServiceMultipleItem.TYPE_SERVICE_REPLY_PROBLEM,
            R.layout.adapter_service_type_service_reply_layout
        )//客服回复
        addItemType(
            ServiceMultipleItem.TYPE_PEOPLE_SERVICE,
            R.layout.adapter_service_type_people_service_layout
        )//人工服务

    }

    override fun convert(helper: BaseViewHolder, item: ServiceMultipleItem) {
        when (helper.itemViewType) {
            ServiceMultipleItem.TYPE_SERVICE_PROBLEM_LIST -> {
                val list = item.data as List<*>
                val llProblemList = helper.getView<LinearLayout>(R.id.llProblemList)
                val ivAvatar = helper.getView<ImageView>(R.id.iv_avatar)
                llProblemList.removeAllViews()
                list.forEach {
                    if (it != null && it is ServiceProblemModel) {
                        val textView = getProblemItemView(context, buildString {
                            append(list.indexOf(it) + 1)
                            append(".")
                            append(it.title)
                        })
                        textView.setOnClickListener { _ ->
                            mClickProblemItemCallBack?.invoke(it)
                        }
                        llProblemList.addView(textView)
                    }
                }
                helper.getView<TextView>(R.id.tvPeopleService).setOnClickListener {
                    //人工服务
                    mConnectPeopleServiceCallBack?.invoke()
                }
                ivAvatar.loadCircleImage(
                    BASE_URL.substring(
                        0,
                        BASE_URL.length - 1
                    ) + mAvatarUrl,
                    holder = R.mipmap.icon_default_header
                )
            }
            ServiceMultipleItem.TYPE_USER_SEND_PROBLEM -> {
                val text = item.data as String
                val ivAvatar = helper.getView<ImageView>(R.id.iv_avatar)
                val tvContent = helper.getView<TextView>(R.id.tv_content)
                SimpleCommonUtils.spannableEmoticonFilter(tvContent, text)
                ivAvatar.loadCircleImage(
                    BASE_URL.substring(
                        0,
                        BASE_URL.length - 1
                    ) + BaseApplication.instance().mUserModel?.headUrl,
                    holder = R.mipmap.icon_default_header
                )
            }
            ServiceMultipleItem.TYPE_SERVICE_REPLY_PROBLEM -> {
                val content = item.data as ProblemStatusModel
                val ivAvatar = helper.getView<ImageView>(R.id.iv_avatar)
                val tvContent = helper.getView<TextView>(R.id.tvContent)
                val tvSolve = helper.getView<TextView>(R.id.tvSolve)
                val tvUnSolve = helper.getView<TextView>(R.id.tvUnSolve)
                val rlSolve = helper.getView<RelativeLayout>(R.id.rlSolve)
                val rlUnSolve = helper.getView<RelativeLayout>(R.id.rlUnSolve)
                tvContent.text = content.problemModel.content
                //已解决
                rlSolve.setOnClickListener { _ ->
                    if (mClickAnswerItemCallBack != null && content.status == 0) {
                        tvSolve.setTextColor(context.resources.getColor(R.color.defaultTextColor))
                        tvUnSolve.setTextColor(context.resources.getColor(R.color.hintTextColor))
                        content.status = 1
                        mClickAnswerItemCallBack?.invoke(content)
                    }

                }
                //未解决
                rlUnSolve.setOnClickListener { _ ->
                    if (mClickAnswerItemCallBack != null && content.status == 0) {
                        tvSolve.setTextColor(context.resources.getColor(R.color.hintTextColor))
                        tvUnSolve.setTextColor(context.resources.getColor(R.color.defaultTextColor))
                        content.status = -1
                        mClickAnswerItemCallBack?.invoke(content)

                    }
                }
                ivAvatar.loadCircleImage(
                    BASE_URL.substring(
                        0,
                        BASE_URL.length - 1
                    ) + mAvatarUrl,
                    holder = R.mipmap.icon_default_header
                )
            }
            ServiceMultipleItem.TYPE_PEOPLE_SERVICE -> {
                helper.getView<TextView>(R.id.tvPeopleService).setOnClickListener {
                    //人工服务
                    mConnectPeopleServiceCallBack?.invoke()
                }
            }
        }
    }

    private fun getProblemItemView(context: Context, content: String): TextView {
        return TextView(context).apply {
            textSize = 15f
            setPadding(0, 8, 0, 8)
            setTextColor(context.resources.getColor(R.color.colorAccent))
            text = content
        }

    }
}