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
import com.czt.mp3recorder.MP3Recorder
import com.google.gson.Gson
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.SEND_SERVICE_MESSAGE_TIME_KEY
import com.guangzhida.xiaomai.SERVICE_USERNAME
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.hideKeyboard
import com.guangzhida.xiaomai.model.AccountModel
import com.guangzhida.xiaomai.model.ProblemStatusModel
import com.guangzhida.xiaomai.model.ServiceProblemModel
import com.guangzhida.xiaomai.room.entity.UserEntity
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
import kotlinx.android.synthetic.main.activity_online_service_layout.emoticonsBoard
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
class ServiceActivity : BaseActivity<ServiceViewModel>(), FuncLayout.FuncKeyBoardListener {
    //存储本地绑定的账号信息
    private var mSchoolAccountInfoGson by Preference(Preference.SCHOOL_NET_ACCOUNT_GSON, "")
    private val mDatas = mutableListOf<ServiceMultipleItem>()
    private val mListServiceProblems = mutableListOf<String>()
    private val mListServiceProblems2 = mutableListOf<ServiceProblemModel>()
    //问题列表的适配器
    private val mAdapter by lazy {
        ServiceProblemAdapter(mDatas)
    }
    private val mAccountModel by lazy {
        Gson().fromJson(mSchoolAccountInfoGson, AccountModel::class.java)
    }
    private val mHandler by lazy {
        Handler()
    }
    //监听键盘发送事件
    private val onEmoticonClickListener = OnEmoticonClickListener<Emoticon> {
        when (it) {
            is DeleteEmoticon -> {
                val action = KeyEvent.ACTION_DOWN
                val code = KeyEvent.KEYCODE_DEL
                val event = KeyEvent(action, code)
                emoticonsBoard.etChat.onKeyDown(KeyEvent.KEYCODE_DEL, event)
            }
            is PlaceHoldEmoticon -> { // do nothing
            }
            is BigEmoticon -> {

            }
            else -> {
                val content: String? = it.code
                if (!content.isNullOrEmpty()) {
                    val index: Int = emoticonsBoard.etChat.selectionStart
                    val editable: Editable = emoticonsBoard.etChat.text
                    editable.insert(index, content)
                }
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_online_service_layout

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val layoutParams =
            toolbar.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(
            layoutParams.leftMargin,
            layoutParams.topMargin + getStatusBarHeight(),
            layoutParams.rightMargin,
            layoutParams.bottomMargin
        )
        initChatBoard()
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
            jumpToPeopleService()
        }
        recyclerView.adapter = mAdapter
        mViewModel.getServiceProblemList()
        //点击RecyclerView隐藏键盘
        recyclerView.setOnTouchListener { _, _ ->
            emoticonsBoard.reset()
            return@setOnTouchListener false
        }
    }

    /**
     * 跳转到人工客服
     */
    private fun jumpToPeopleService() {
        val time = SPUtils.get(this, SEND_SERVICE_MESSAGE_TIME_KEY, 0L) as Long
        val curTime = DateUtils.getNow().time
        val intent = Intent(this, ChatMessageActivity::class.java)
        intent.putExtra("userName", SERVICE_USERNAME)
        if (curTime - time > (1 * 60 * 60 * 1000)) {
            SPUtils.put(this, SEND_SERVICE_MESSAGE_TIME_KEY, curTime)
            intent.putExtra("ServiceMessage", buildString {
                append("----用户信息----\n")
                append("账号:")
                append(mAccountModel?.user ?: "未绑定账号")
                append("\n")
                append("密码:")
                append(mAccountModel?.pass ?: "未绑定账号")
                append("\n")
                append("账号类型:")
                append(mAccountModel?.servername ?: "未绑定账号")
                append("\n")
                append("学校:")
                append(mAccountModel?.name ?: "未选择学校")
                append("\n")
                append("wifi名称:")
                append(NetworkUtils.getWifiName(this@ServiceActivity))
                append("\n")
                append("IP地址:")
                append(NetworkUtils.getIPAddress(true))
            })
        }
        startActivity(intent)
        finish()

    }

    override fun initListener() {
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

    }

    /**
     * 初始化底部聊天板
     */
    private fun initChatBoard() {
        emoticonsBoard.etChat.addEmoticonFilter(EmojiFilter())
        val adapter = AdapterUtils.getCommonAdapter(this, onEmoticonClickListener)
        emoticonsBoard.setAdapter(adapter)
        emoticonsBoard.addOnFuncKeyBoardListener(this)
        val simpleAppsGridView = SimpleAppsGridView(this)
        emoticonsBoard.addFuncView(simpleAppsGridView)
        emoticonsBoard.etChat.setOnSizeChangedListener { _, _, _, _ -> scrollToBottom() }
        emoticonsBoard.btnSend.setOnClickListener {
            LogUtils.i("点击发送")
            val content = emoticonsBoard.etChat.text.toString().trim()
            if (content.isNotEmpty()) {
                LogUtils.e("发送消息=$content")
                emoticonsBoard.etChat.setText("")
                val multipleItem =
                    ServiceMultipleItem(ServiceMultipleItem.TYPE_USER_SEND_PROBLEM, content)
                mDatas.add(multipleItem)
                mAdapter.notifyDataSetChanged()
                if (mListServiceProblems.contains(content)) {
                    val model = mListServiceProblems2[mListServiceProblems.indexOf(content)]
                    replayProblem(model)
                } else {
                    val index = content.toIntOrNull()
                    if (index != null && index <= mListServiceProblems2.size) {
                        val model = mListServiceProblems2[index - 1]
                        replayProblem(model)
                    }
                }
                scrollToBottom()
            }
        }
        if (checkPermission(
                arrayListOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        ) {
            emoticonsBoard.setChatSoundRecordPressedViewShowDialog(true)
        }
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
                    ServiceMultipleItem.TYPE_SERVICE_REPLY_PROBLEM,
                    problemStatusModel
                )
            )
            mAdapter.notifyItemInserted(mDatas.size - 1)
        }, 1000)
    }

    /**
     * 滚动到底部
     */
    private fun scrollToBottom() {
        recyclerView.post { recyclerView.smoothScrollToPosition(mAdapter.itemCount - 1) }
    }

    override fun onFuncClose() {
    }

    override fun onFuncPop(height: Int) {
        scrollToBottom()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (EmoticonsKeyboardUtils.isFullScreen(this)) {
            if (emoticonsBoard.dispatchKeyEventInFullScreen(event)) {
                true
            } else {
                super.dispatchKeyEvent(event)
            }
        } else super.dispatchKeyEvent(event)
    }
}