package com.guangzhida.xiaomai.ui.chat

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Editable
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.czt.mp3recorder.MP3Recorder
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.SEND_SERVICE_MESSAGE_TIME_KEY
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.dialog.ServerHelpDialog
import com.guangzhida.xiaomai.ext.hideKeyboard
import com.guangzhida.xiaomai.ext.jumpLoginByState
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.ProblemStatusModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.model.ServiceProblemModel
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.service.CheckNetWorkStateService
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMessageAdapter
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMultipleItem
import com.guangzhida.xiaomai.ui.chat.adapter.ServiceMultipleItem
import com.guangzhida.xiaomai.ui.chat.adapter.ServiceProblemAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.ServiceViewModel
import com.guangzhida.xiaomai.utils.*
import com.guangzhida.xiaomai.view.chat.*
import com.guangzhida.xiaomai.view.custom.CustomImgPickerPresenter
import com.hyphenate.chat.EMImageMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMVoiceMessageBody
import com.ypx.imagepicker.ImagePicker
import com.ypx.imagepicker.bean.MimeType
import com.ypx.imagepicker.bean.SelectMode
import com.ypx.imagepicker.bean.selectconfig.CropConfig
import github.ll.emotionboard.data.Emoticon
import github.ll.emotionboard.interfaces.OnEmoticonClickListener
import github.ll.emotionboard.utils.EmoticonsKeyboardUtils
import github.ll.emotionboard.widget.FuncLayout
import kotlinx.android.synthetic.main.activity_chat_message.*
import kotlinx.android.synthetic.main.activity_online_service_layout.*
import kotlinx.android.synthetic.main.activity_online_service_layout.recyclerView
import kotlinx.android.synthetic.main.activity_online_service_layout.toolbar
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

/**
 * 客服聊天界面
 */
class ServiceActivity : BaseActivity<ServiceViewModel>() {
    //存储本地绑定的账号信息
    private var mSchoolSelectInfoGson by Preference(Preference.SCHOOL_SELECT_INFO_GSON, "")
    private var mSchoolModel: SchoolModel? = null
    private val mDatas = mutableListOf<ServiceMultipleItem>()
    private val mListServiceProblems = mutableListOf<String>()
    private val mListServiceProblems2 = mutableListOf<ServiceProblemModel>()
    //问题列表的适配器
    private val mAdapter by lazy {
        ServiceProblemAdapter(mDatas)
    }
    private val mHandler by lazy {
        Handler()
    }
    private val mGson by lazy {
        Gson()
    }

    override fun layoutId(): Int = R.layout.activity_online_service_layout

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mSchoolModel = mGson.fromJson<SchoolModel>(mSchoolSelectInfoGson, SchoolModel::class.java)
        val layoutParams =
            toolbar.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(
            layoutParams.leftMargin,
            layoutParams.topMargin + getStatusBarHeight(),
            layoutParams.rightMargin,
            layoutParams.bottomMargin
        )
        toolbar.setNavigationOnClickListener {
            finish()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        (recyclerView.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        mAdapter.mClickProblemItemCallBack = {
            val multipleItem =
                ServiceMultipleItem(ServiceMultipleItem.TYPE_USER_SEND_PROBLEM, it.title)
            mDatas.add(multipleItem)
            mAdapter.notifyDataSetChanged()
            //根据选择问题回复解决列表
            replayProblem(it)
        }
        //点击已解决未解决
        mAdapter.mClickAnswerItemCallBack = {
            if (it.status == -1) {
                val item = ServiceMultipleItem(ServiceMultipleItem.TYPE_PEOPLE_SERVICE, null)
                mDatas.add(item)
                mAdapter.notifyItemInserted(mDatas.size)
            }
        }
        //转人工
        mAdapter.mConnectPeopleServiceCallBack = {
            connectPeopleService()
        }
        //点击帮助item
        mAdapter.mClickHelpItemCallBack = {
            ServerHelpDialog.showDialog(this)
        }
        val mServerSendMultipleItem =
            ServiceMultipleItem(ServiceMultipleItem.TYPE_SERVICE_SEND, "您好请问您有什么问题需要帮助？")
//        val mHelpMultipleItem =
//            ServiceMultipleItem(ServiceMultipleItem.TYPE_SERVICE_HELP_LIST, null)
        mDatas.add(mServerSendMultipleItem)
//        mDatas.add(mHelpMultipleItem)
        recyclerView.adapter = mAdapter
        mViewModel.getServiceProblemList()

    }

    /**
     * 连接人工客服
     * 首先检测网络状态
     */
    private fun connectPeopleService() {
        if (BaseApplication.instance().mUserModel != null) {
            if (mSchoolModel == null) {
                ToastUtils.toastShort("请先选择学校")
            } else {
                mViewModel.searchOnlineService(mSchoolModel!!.id)
            }
            //开启后台Service进行网络状况诊断
            //查询在线的客服 (后台动态分配客服)
        } else {
            jumpLoginByState()
        }
    }


    override fun initListener() {
        ivConnectPeopleServer.clickN {
            connectPeopleService()
        }
        // 获取到问题列表
        mViewModel.mServiceProblemListModel.observe(this, Observer {
            mListServiceProblems2.clear()
            mListServiceProblems.clear()
            it.forEach { model ->
                mListServiceProblems2.add(model)
                mListServiceProblems.add(model.title)
            }
            val multipleItem =
                ServiceMultipleItem(ServiceMultipleItem.TYPE_SERVICE_PROBLEM_LIST, it)
            mDatas.add(multipleItem)
            mAdapter.notifyItemInserted(mDatas.size - 1)
        })

        mViewModel.mServiceResult.observe(this, Observer {
            if (it == null) {
                ToastUtils.toastShort("暂无在线客服")
            } else {
                val intent = Intent(this, CheckNetWorkStateService::class.java)
                intent.putExtra("userName", BaseApplication.instance().mUserModel?.username)
                intent.putExtra("serverName", it.username)
                startService(intent)
                startKtxActivity<ChatServiceActivity>(
                    value = Pair(
                        "serviceModel",
                        mGson.toJson(it)
                    )
                )
            }
        })


    }


    /**
     * 回复问题
     */
    private fun replayProblem(model: ServiceProblemModel) {
        //根据选择问题回复解决列表
        mHandler.postDelayed({
            val problemStatusModel = ProblemStatusModel(model, 0)
            mDatas.add(
                ServiceMultipleItem(
                    ServiceMultipleItem.TYPE_SERVICE_REPLY_PROBLEM, problemStatusModel
                )
            )
            mAdapter.notifyItemInserted(mDatas.size - 1)
        }, 1000)
    }

}