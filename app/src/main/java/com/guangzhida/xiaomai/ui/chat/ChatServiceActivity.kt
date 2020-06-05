package com.guangzhida.xiaomai.ui.chat

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.czt.mp3recorder.MP3Recorder
import com.google.gson.Gson
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.model.ServiceModel
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMessageAdapter
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMultipleItem
import com.guangzhida.xiaomai.ui.chat.viewmodel.ChatServiceMessageViewModel
import com.guangzhida.xiaomai.ui.login.LoadingActivity
import com.guangzhida.xiaomai.utils.AdapterUtils
import com.guangzhida.xiaomai.utils.AudioPlayManager
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.view.chat.*
import com.guangzhida.xiaomai.view.custom.CustomImgPickerPresenter
import com.guangzhida.xiaomai.view.preview.PreviewResultListActivity
import com.hyphenate.chat.EMImageMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMVoiceMessageBody
import com.hyphenate.util.EasyUtils
import com.ypx.imagepicker.ImagePicker
import com.ypx.imagepicker.bean.MimeType
import com.ypx.imagepicker.bean.SelectMode
import com.ypx.imagepicker.bean.selectconfig.CropConfig
import github.ll.emotionboard.data.Emoticon
import github.ll.emotionboard.interfaces.OnEmoticonClickListener
import github.ll.emotionboard.utils.EmoticonsKeyboardUtils
import github.ll.emotionboard.widget.FuncLayout
import kotlinx.android.synthetic.main.activity_chat_service_message.*
import permissions.dispatcher.ktx.withPermissionsCheck
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File


/**
 * 聊天界面
 */
class ChatServiceActivity : BaseActivity<ChatServiceMessageViewModel>(),
    FuncLayout.FuncKeyBoardListener {
    override fun layoutId(): Int = R.layout.activity_chat_service_message
    private var mMessage: String? = null  // 网络诊断信息
    private var mUserName: String? = null
    private var mFriendAvatar: String? = null //好友头像
    private var mStartRecorderTime = 0L //开始录音的时间
    private var mCurPlayChatMultipleItem: ChatMultipleItem? = null //保存点击播放语音的item
    private lateinit var mAdapter: ChatMessageAdapter
    private val mRecordSaveFile by lazy {
        File(
            getExternalFilesDir("voice"),
            "chatSoundRecord.mp3"
        )
    }
    private val mImgSaveDir by lazy {
        getExternalFilesDir("pic")?.absolutePath
            ?: Environment.getExternalStorageDirectory().absolutePath + "/xiaomai/pic"
    }
    private val mMP3Recorder by lazy {
        MP3Recorder(mRecordSaveFile)
    }
    private val mChatMultipleItemList = mutableListOf<ChatMultipleItem>()
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
    //录音的回调
    private val mSoundRecordCallBack = object : SimpleUserEmoticonsBoard.OnSoundRecordCallBack {
        override fun onCancelRecord() {
            mMP3Recorder.stop()
            mRecordSaveFile.deleteOnExit()
        }

        override fun onStartRecord() {
            startSoundRecord()
        }

        override fun onStopRecord() {
            mMP3Recorder.stop()
            if (mRecordSaveFile.exists()) {
                //发送语音
                sendVoiceMsg()
            } else {
                ToastUtils.toastShort("录音文件找不到")
            }
        }
    }
    //键盘中加号里面的扩展view的点击回调
    private val mFuncViewCallBack = ChattingAppsAdapter.OnChattingAppsCallBack {
        when (it.funcName) {
            SimpleAppsGridView.FUNC_PLAY_PHOTO -> {
                //拍照
                takePhoto()
            }
            SimpleAppsGridView.FUNC_SELECT_PHOTO -> {
                //选择图片
                selectPhoto()
            }
        }
    }
    //
    private val mAudioPlayCallBack = object : AudioPlayManager.IAudioPlayListener {
        override fun onComplete(uri: Uri?) {
            uri?.let {
                val item = getVoiceChatMultipleItem(it)
                item?.isVoicePlay = false
                mAdapter.notifyItemChanged(mChatMultipleItemList.indexOf(item))
            }
        }

        override fun onStop(uri: Uri?) {
            uri?.let {
                val item = getVoiceChatMultipleItem(it)
                item?.isVoicePlay = false
                mAdapter.notifyItemChanged(mChatMultipleItemList.indexOf(item))
            }
        }

        override fun onStart(uri: Uri?) {
            uri?.let {
                val item = getVoiceChatMultipleItem(it)
                item?.isVoicePlay = true
                mAdapter.notifyItemChanged(mChatMultipleItemList.indexOf(item))
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        val layoutParams =
            toolbar.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(
            layoutParams.leftMargin,
            layoutParams.topMargin + getStatusBarHeight(),
            layoutParams.rightMargin,
            layoutParams.bottomMargin
        )
        val serviceModel = Gson().fromJson<ServiceModel>(
            intent.getStringExtra("serviceModel"),
            ServiceModel::class.java
        )
        if (serviceModel == null) {
            ToastUtils.toastShort("客服对象为空")
            finish()
            return
        }
        mUserName = serviceModel.username ?: serviceModel.mobilePhone
        mFriendAvatar = serviceModel.headUrl
        tvFriendName.text = serviceModel.nickName
        initChatBoard()
        initRecyclerView()
        initLiveDataObserver()
        mViewModel.init(serviceModel)
    }

    override fun onNewIntent(intent: Intent) { // make sure only one chat activity is opened
        val username = intent.getStringExtra("userName")
        if (mUserName == username) {
            mViewModel.pullNewMessage()
            scrollToBottom()
            super.onNewIntent(intent)
        } else {
            finish()
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (EasyUtils.isSingleActivity(this)) {
            startActivity(Intent(this, LoadingActivity::class.java))
        } else {
            super.onBackPressed()
        }
    }

    /**
     * 初始化环信的数据
     * 根据聊天的id加载会话信息
     */
    private fun initRecyclerView() {
        //下拉刷新
        swipeRefreshLayout.setOnRefreshListener {
            mViewModel.loadMoreMessage()
        }
        mAdapter = ChatMessageAdapter(mChatMultipleItemList, mFriendAvatar, this)
        mAdapter.mImageCallBack = {
            startPreviewImg(it)
        }
        //点击语音
        mAdapter.mVoiceClickCallBack = { _, item ->
            val url = getAudioPlayUri(item)
            if (url.isNotEmpty()) {
                val uri = Uri.parse(url)
                if (item == mCurPlayChatMultipleItem) {
                    AudioPlayManager.getInstance().playOrPause(this, uri, mAudioPlayCallBack)
                } else {
                    mCurPlayChatMultipleItem = item
                    AudioPlayManager.getInstance().startPlay(this, uri, mAudioPlayCallBack)
                }
            } else {
                ToastUtils.toastShort("url问题,播放出错")
            }
        }
        //删除聊天记录
        mAdapter.mDeleteMessageCallBack = {
            mViewModel.deleteChatMessage(it.mMessage)
            mChatMultipleItemList.remove(it)
            mAdapter.notifyDataSetChanged()
        }
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        swipeRefreshLayout.isEnabled = false
        //设置layoutManager
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        (recyclerView.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        recyclerView.adapter = mAdapter
        //点击SwipeRefreshLayout隐藏键盘
        recyclerView.setOnTouchListener { _, _ ->
            emoticonsBoard.reset()
            return@setOnTouchListener false
        }
        //点击topBar的返回键
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * 获取语音播放的uri
     */
    private fun getAudioPlayUri(item: ChatMultipleItem): String {
        val audioMessageBody = item.mMessage.body as EMVoiceMessageBody
        LogUtils.i("remoteUrl=${audioMessageBody.remoteUrl}")
        LogUtils.i("localUrl=${audioMessageBody.localUrl}")
        return when {
            audioMessageBody.remoteUrl.isNotEmpty() -> {
                audioMessageBody.remoteUrl
            }
            audioMessageBody.localUrl.isNotEmpty() -> {
                audioMessageBody.localUrl
            }
            else -> {
                ""
            }
        }
    }

    /**
     * 通过uri
     */
    private fun getVoiceChatMultipleItem(uri: Uri): ChatMultipleItem? {
        mChatMultipleItemList.forEach {
            val body = it.mMessage.body
            if (body is EMVoiceMessageBody) {
                val locUri = Uri.parse(getAudioPlayUri(it))
                if (locUri == uri) {
                    return it
                }
            }
        }
        return null
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
                mUserName?.let {
                    mViewModel.sendTextMessage(content)
                }
            }
        }
        emoticonsBoard.setOnSoundRecordCallBack(mSoundRecordCallBack)
        simpleAppsGridView.setOnAppsAdapterCallBack(mFuncViewCallBack)
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
     * 注册监听事件
     */
    private fun initLiveDataObserver() {
        //注册用户资料的监听
        mViewModel.mInitUserInfoObserver.observe(this, Observer {
            tvFriendName.text = it.first
            mFriendAvatar = it.second

        })
        //初始化完成
        mViewModel.mInitConversationLiveData.observe(this, Observer {
            mChatMultipleItemList.clear()
            val list = it.map { emmMessage ->
                ChatMultipleItem(emmMessage)
            }.reversed()
            mChatMultipleItemList.addAll(list)
            mAdapter.notifyDataSetChanged()
            LogUtils.i("mChatMultipleItemList size = ${mChatMultipleItemList.size}")
            val ll = recyclerView.layoutManager as LinearLayoutManager
            ll.scrollToPosition(0)
            swipeRefreshLayout.isEnabled = true
            //发送客服消息
            if (!mMessage.isNullOrEmpty()) {
                mViewModel.sendTextMessage(mMessage!!)
            }
        })
        //刷新结果回调
        mViewModel.refreshResultLiveData.observe(this, Observer {
            swipeRefreshLayout.isRefreshing = false
        })
        //有更多数据
        mViewModel.haveMoreDataLiveData.observe(this, Observer {
            val list = it.map { emmMessage ->
                ChatMultipleItem(emmMessage)
            }.reversed()
            mChatMultipleItemList.addAll(list)
            mAdapter.notifyDataSetChanged()
            recyclerView.post {
                recyclerView.smoothScrollBy(0, -200)
            }
        })
        //发送消息成功
        mViewModel.sendMessageSuccessLiveData.observe(this, Observer {
            val item = ChatMultipleItem(it)
            mChatMultipleItemList.add(0, item)
            mAdapter.notifyItemInserted(0)
            scrollToBottom()
        })
        //接收到消息
        mViewModel.receiveMessageLiveData.observe(this, Observer {
            val item = ChatMultipleItem(it)
            mChatMultipleItemList.add(0, item)
            mAdapter.notifyItemInserted(0)
        })
        //获取用户头像
        mViewModel.mUserAvatarLiveData.observe(this, Observer {
            mAdapter.mUserAvatar = it
            mAdapter.notifyDataSetChanged()
        })
        //删除好友的回调
        mViewModel.deleteFriendLiveData.observe(this, Observer {
            if (it) {
                this.finish()
            }
        })

    }

    /**
     * 滚动到底部
     */
    private fun scrollToBottom() {
        recyclerView.post { recyclerView.smoothScrollToPosition(0) }
    }

    /**
     * 开始录音
     */
    private fun startSoundRecord() = withPermissionsCheck(Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE, onShowRationale = {
            it.proceed()
        }) {
        mStartRecorderTime = System.currentTimeMillis()
        emoticonsBoard.setChatSoundRecordPressedViewShowDialog(true)
        mMP3Recorder.start()
        //启动线程获取录音的音量
        startThreadForVolume()
    }

    /**
     * 拍照
     */
    private fun takePhoto() = withPermissionsCheck(Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE, onShowRationale = {
            it.proceed()
        }) {
        val cropConfig = CropConfig().apply {
            setCropRatio(1, 1)
            cropRectMargin = 100
            saveInDCIM(false)
            isCircle = false
            cropStyle = CropConfig.STYLE_GAP
            cropGapBackgroundColor = Color.TRANSPARENT
        }
        ImagePicker.takePhotoAndCrop(this, CustomImgPickerPresenter(), cropConfig) {
            if (it.isNotEmpty()) {
                uploadImgCompress(listOf(it[0].path))
            }
        };
    }

    /**
     * 选择图片最多选择9张
     */
    private fun selectPhoto() =
        withPermissionsCheck(Manifest.permission.WRITE_EXTERNAL_STORAGE, onShowRationale = {
            it.proceed()
        }) {
            ImagePicker.withMulti(CustomImgPickerPresenter())//指定presenter
                //设置选择的最大数
                .setMaxCount(9)
                //设置列数
                .setColumnCount(4)
                //设置要加载的文件类型，可指定单一类型
                .mimeTypes(MimeType.ofImage())
                .showCamera(false)//显示拍照
                .setPreview(true)//开启预览
                //大图预览时是否支持预览视频
                .setPreviewVideo(false)
                //设置视频单选
                .setVideoSinglePick(false)
                //设置图片和视频单一类型选择
                .setSinglePickImageOrVideoType(true)
                //当单选或者视频单选时，点击item直接回调，无需点击完成按钮
                .setSinglePickWithAutoComplete(false)
                //显示原图
                .setOriginal(true)
                //显示原图时默认原图选项开关
                .setDefaultOriginal(true)
                //设置单选模式，当maxCount==1时，可执行单选（下次选中会取消上一次选中）
                .setSelectMode(SelectMode.MODE_SINGLE)
                .pick(this) {
                    if (it.isNotEmpty()) {
                        val filePathList = it.map { item ->
                            item.path
                        }
                        uploadImgCompress(filePathList)
                    }
                }
        }

    /**
     * 压缩并上传图片
     */
    private fun uploadImgCompress(imagePath: List<String>) {
        Luban.with(this)
            .load(imagePath)
            .ignoreBy(100)
            .setTargetDir(mImgSaveDir)
            .setCompressListener(object : OnCompressListener {
                override fun onSuccess(file: File?) {
                    if (file != null && file.exists()) {
                        mViewModel.sendPicMessage(file)
                    }
                }

                override fun onError(e: Throwable?) {
                }

                override fun onStart() {
                }

            }).launch()
    }

    /**
     * 启动线程获取音量
     */
    private fun startThreadForVolume() {
        Thread {
            while (mMP3Recorder.isRecording) {
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val volume = mMP3Recorder.volume
                runOnUiThread {
                    emoticonsBoard.setVolume(volume)
                }
            }
        }.start()
    }

    /**
     * 预览大图
     */
    private fun startPreviewImg(item: ChatMultipleItem) =
        withPermissionsCheck(Manifest.permission.READ_EXTERNAL_STORAGE, onShowRationale = {
            it.proceed()
        }) {
            val imageItemList = arrayListOf<String>()
            var pos = 0
            mChatMultipleItemList.filter {
                it.mMessage.type == EMMessage.Type.IMAGE
            }.forEachIndexed { index, chatMultipleItem ->
                if (chatMultipleItem == item) {
                    pos = index
                }
                val imgBody = chatMultipleItem.mMessage.body as EMImageMessageBody
                val path = if (File(imgBody.localUrl).exists()) {
                    imgBody.localUrl
                } else {
                    imgBody.remoteUrl
                }
                imageItemList.add(path)
            }
            imageItemList.reverse()
            val intent = Intent(
                this,
                PreviewResultListActivity::class.java
            )
            intent.putStringArrayListExtra("imgUrls", imageItemList)
            intent.putExtra("pos", imageItemList.size - 1 - pos)
            startActivity(intent)
        }

    /**
     * 发送文件
     */
    private fun sendVoiceMsg() {
        //判断文件是否还在被写入
        var oldLength = 0L
        do {
            oldLength = mRecordSaveFile.length()
            try {
                Thread.sleep(200)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } while (oldLength != mRecordSaveFile.length())
        val timeLen = System.currentTimeMillis() - mStartRecorderTime
        if (timeLen < 1000) {
            ToastUtils.toastShort("录音时长太短")
            return
        }
        mViewModel.sendVoiceMessage(
            mRecordSaveFile,
            timeLen
        )
    }

    override fun onResume() {
        super.onResume()
        mViewModel.addListener()
    }

    override fun onPause() {
        super.onPause()
        mViewModel.removeListener()
        emoticonsBoard.reset()
        AudioPlayManager.getInstance().stopPlay()
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

    override fun onFuncClose() {
    }

    override fun onFuncPop(height: Int) {
        scrollToBottom()
    }

}