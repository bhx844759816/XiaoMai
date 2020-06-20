package com.guangzhida.xiaomai.ui.chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
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
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
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
    var mClickHelpItemCallBack: ((Int) -> Unit)? = null //点击帮助列表

    init {
        addItemType(
            ServiceMultipleItem.TYPE_SERVICE_HELP_LIST,
            R.layout.adapter_service_type_help_layout
        )//帮助列表
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
        addItemType(
            ServiceMultipleItem.TYPE_SERVICE_SEND,
            R.layout.adapter_service_type_service_send_layout
        )
    }

    override fun convert(helper: BaseViewHolder, item: ServiceMultipleItem) {
        when (helper.itemViewType) {
            ServiceMultipleItem.TYPE_SERVICE_HELP_LIST -> {
                helper.getView<TextView>(R.id.ivHelpItemOne).clickN {
                    mClickHelpItemCallBack?.invoke(0)
                }
                helper.getView<TextView>(R.id.ivHelpItemTwo).clickN {
                    mClickHelpItemCallBack?.invoke(1)
                }
                helper.getView<TextView>(R.id.ivHelpItemThree).clickN {
                    mClickHelpItemCallBack?.invoke(2)
                }
                helper.getView<TextView>(R.id.ivHelpItemFour).clickN {
                    mClickHelpItemCallBack?.invoke(3)
                }
                helper.getView<TextView>(R.id.ivHelpItemFive).clickN {
                    mClickHelpItemCallBack?.invoke(4)
                }
            }
            ServiceMultipleItem.TYPE_SERVICE_SEND -> {
                val content = item.data as String
                helper.setText(R.id.tv_content, content)
            }
            ServiceMultipleItem.TYPE_SERVICE_PROBLEM_LIST -> {
                val list = item.data as List<*>
                val llProblemList = helper.getView<LinearLayout>(R.id.llProblemList)
                val ivAvatar = helper.getView<ImageView>(R.id.iv_avatar)
                llProblemList.removeAllViews()
                list.forEach {
                    if (it != null && it is ServiceProblemModel) {
                        val textView = getProblemItemView2(context, it.title)
                        textView.setOnClickListener { _ ->
                            mClickProblemItemCallBack?.invoke(it)
                        }
                        llProblemList.addView(textView)
                    }
                }
                helper.getView<TextView>(R.id.tvChangeProblemList).setOnClickListener {
                }
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
            textSize = 13f
            setPadding(0, 8, 0, 8)

            setTextColor(context.resources.getColor(R.color.colorAccent))
            text = content
        }
    }

    private fun getProblemItemView2(context: Context, content: String): View {
        val ll = LayoutInflater.from(context).inflate(R.layout.view_problem_item_layout, null)
        val tvContent = ll.findViewById<TextView>(R.id.tvContent)
        tvContent.text = content
        return ll
    }

}