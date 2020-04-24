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
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.SERVICE_USERNAME
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.dialog.DeleteFriendDialog
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.room.AppDatabase
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMessageAdapter
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMultipleItem
import com.guangzhida.xiaomai.ui.chat.viewmodel.ChatMessageViewModel
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
import kotlinx.android.synthetic.main.activity_chat_message.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File


/**
 * 聊天界面
 */
@RuntimePermissions
open class ChatMessageActivity : BaseActivity<ChatMessageViewModel>(),
    FuncLayout.FuncKeyBoardListener {
    override fun layoutId(): Int = R.layout.activity_chat_message
    private var mFriendId: String? = null
    private var mState = 0 // 0为默认状态 1为从查询聊天记录进来的
    var mUserName: String? = null
    var mMessage: String? = null
    private var mFriendAvatar: String? = null
    private var mStartRecorderTime = 0L //开始录音的时间
    private var mChatUserEntity: UserEntity? = null
    private var mCurPlayChatMultipleItem: ChatMultipleItem? = null
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

            startSoundRecordWithPermissionCheck()
        }

        override fun onStopRecord() {
            mMP3Recorder.stop()
            if (mFriendId != null && mRecordSaveFile.exists() && mUserName != null) {
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
                takePhotoWithPermissionCheck()
            }
            SimpleAppsGridView.FUNC_SELECT_PHOTO -> {
                //选择图片
                selectPhotoWithPermissionCheck()
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
        val userName = intent.getStringExtra("userName")

        mMessage = intent.getStringExtra("ServiceMessage")
        mState = intent.getIntExtra("State", 0)
        if (userName != null && userName.isNotEmpty()) {
            mUserName = userName
        }
        //查询本地数据库中
        mUserName?.let {
            if (it == SERVICE_USERNAME) {
                rlChatSelect.visibility = View.GONE
                BaseApplication.instance().mServiceModel?.let { chatUserModel ->
                    val entity = UserEntity(uid = chatUserModel.id.toLong())
                    entity.userName = chatUserModel.mobilePhone
                    entity.avatarUrl = chatUserModel.headUrl ?: ""
                    entity.sex = chatUserModel.sex.toString()
                    entity.age = chatUserModel.age.toString()
                    entity.nickName = chatUserModel.nickName
                    mChatUserEntity = entity
                }
            } else {
                rlChatSelect.visibility = View.VISIBLE
                mChatUserEntity = AppDatabase.invoke(this).userDao()?.queryUserByUserName(it)
                if (mChatUserEntity == null) {
                    val conversation =
                        AppDatabase.invoke(this).conversationDao()?.queryConversationByUserName(it)
                    if (conversation == null) {
                        ToastUtils.toastShort("好友本地不存在")
                        finish()
                        return
                    }
                    mChatUserEntity = UserEntity(
                        uid = conversation.id,
                        nickName = conversation.nickName,
                        userName = conversation.userName,
                        remarkName = conversation.remarkName,
                        sex = conversation.sex,
                        age = conversation.age,
                        avatarUrl = conversation.avatarUrl
                    )
                }
            }
            mFriendId = mChatUserEntity?.uid.toString()
            tvFriendName.text = mChatUserEntity?.nickName
            mFriendAvatar = mChatUserEntity?.avatarUrl
            mViewModel.init(it)
            if (mState == 0) {
                mViewModel.initLocalMessage()
            }
            initRecyclerView()
            initChatBoard()
            initLiveDataObserver()
            initMessageRecord()
        }

    }

    /**
     * 初始化查询聊天记录的列表
     */
    private fun initMessageRecord() {
        if (mState == 1) {
            val list = intent.getSerializableExtra("EMMessageList") as List<EMMessage>
            mViewModel.initLocalMessage(list[list.size-1].msgId, list.size)
            mChatMultipleItemList.clear()
            val chatList = list.map { emmMessage ->
                ChatMultipleItem(emmMessage)
            }.reversed()
            mChatMultipleItemList.addAll(chatList)
            mAdapter.notifyDataSetChanged()
            swipeRefreshLayout.isEnabled = true
        }
    }

    override fun onNewIntent(intent: Intent) { // make sure only one chat activity is opened
        val username = intent.getStringExtra("userName")
        if (mUserName == username) super.onNewIntent(intent) else {
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
        mAdapter = ChatMessageAdapter(mChatMultipleItemList, mFriendAvatar)
        mAdapter.mImageCallBack = {
            startPreviewImgWithPermissionCheck(it)
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
        //点击头像跳转到联系人详情
        mAdapter.mHeaderClickCallBack = {
            mChatUserEntity?.let {
                startKtxActivity<PersonInfoActivity>(
                    values = listOf(
                        Pair("State", 1),
                        Pair("UserEntityGson", Gson().toJson(it))
                    )
                )
            }
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
        rlChatSelect.setOnClickListener {
            DeleteFriendDialog.showDialog(this, this) {
                if (mFriendId != null && mUserName != null) {
                    mViewModel.deleteFriends(mFriendId!!, mUserName!!)
                }
            }
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
            if (content.isNotEmpty() && mFriendId != null) {
                LogUtils.e("发送消息=$content")
                emoticonsBoard.etChat.setText("")
                mUserName?.let {
                    mViewModel.sendTextMessage(mFriendId!!, content, it)
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
        //初始化完成
        mViewModel.mInitConversationLiveData.observe(this, Observer {
            mChatMultipleItemList.clear()
            val list = it.map { emmMessage ->
                ChatMultipleItem(emmMessage)
            }.reversed()
            mChatMultipleItemList.addAll(list)
            mAdapter.notifyDataSetChanged()
            val ll = recyclerView.layoutManager as LinearLayoutManager
            ll.scrollToPosition(0)
            swipeRefreshLayout.isEnabled = true
            //发送客服消息
            if (!mMessage.isNullOrEmpty()) {
                mUserName?.let { userName ->
                    mViewModel.sendTextMessage(mFriendId!!, mMessage!!, userName)
                }
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
     * 录音
     */
    @NeedsPermission(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    fun startSoundRecord() {
        LogUtils.i("startSoundRecord")
        mStartRecorderTime = System.currentTimeMillis()
        emoticonsBoard.setChatSoundRecordPressedViewShowDialog(true)
        mMP3Recorder.start()
        //启动线程获取录音的音量
        startThreadForVolume()
    }

    /**
     * 拍照
     */
    @NeedsPermission(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    fun takePhoto() {
        val cropConfig = CropConfig().apply {
            setCropRatio(1, 1)
            cropRectMargin = 100
            saveInDCIM(false)
            isCircle = false
            cropStyle = CropConfig.STYLE_GAP
            cropGapBackgroundColor = Color.TRANSPARENT
        }
        ImagePicker.takePhotoAndCrop(this, CustomImgPickerPresenter(), cropConfig) {
            if (it.isNotEmpty() && mFriendId != null && mUserName != null) {
                uploadImgCompress(it[0].path)
            }
        };
    }


    /**
     * 选择图片
     */
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun selectPhoto() {
        ImagePicker.withMulti(CustomImgPickerPresenter())//指定presenter
            //设置选择的最大数
            .setMaxCount(1)
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
                if (it.isNotEmpty() && mFriendId != null && mUserName != null) {
                    uploadImgCompress(it[0].path)
                }
            }
    }

    /**
     * 压缩并上传图片
     */
    private fun uploadImgCompress(imagePath: String) {
        Luban.with(this)
            .load(imagePath)
            .ignoreBy(100)
            .setTargetDir(mImgSaveDir)
            .setCompressListener(object : OnCompressListener {
                override fun onSuccess(file: File?) {
                    if (file != null && file.exists()) {
                        mViewModel.sendPicMessage(mFriendId!!, file, mUserName!!)
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
    @NeedsPermission(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    fun startPreviewImg(item: ChatMultipleItem) {
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
        val intent = Intent(
            this,
            PreviewResultListActivity::class.java
        )
        intent.putStringArrayListExtra("imgUrls", imageItemList)
        intent.putExtra("pos", pos)
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
            mFriendId!!,
            mRecordSaveFile,
            timeLen,
            mUserName!!
        )
    }

    @OnPermissionDenied(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    fun onPermissionDenied() {
        ToastUtils.toastShort("权限被拒绝")
    }

    @OnNeverAskAgain(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    fun onPermissionNeverAskAgain() {
        ToastUtils.toastShort("权限被拒绝且不再提醒")
    }


    override fun onResume() {
        super.onResume()

        mViewModel.addListener()
    }

    override fun onPause() {
        super.onPause()
        emoticonsBoard.reset()
        mViewModel.removeListener()
        AudioPlayManager.getInstance().stopPlay()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
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