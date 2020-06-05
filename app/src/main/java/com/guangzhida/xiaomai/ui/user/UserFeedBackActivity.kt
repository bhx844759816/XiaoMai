package com.guangzhida.xiaomai.ui.user

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.ext.isPhone
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.dp2px
import com.guangzhida.xiaomai.ktxlibrary.ext.listener.textWatcher
import com.guangzhida.xiaomai.ktxlibrary.ext.logd
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.ui.home.adapter.PhotoMultipleItem
import com.guangzhida.xiaomai.ui.user.adapter.FeedBackPhotoAdapter
import com.guangzhida.xiaomai.ui.user.viewmodel.UserFeedBackViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.view.SpacesItemDecoration
import com.guangzhida.xiaomai.view.custom.CustomImgPickerPresenter
import com.guangzhida.xiaomai.view.preview.PreviewResultListActivity
import com.ypx.imagepicker.ImagePicker
import com.ypx.imagepicker.bean.MimeType
import com.ypx.imagepicker.bean.SelectMode
import kotlinx.android.synthetic.main.activity_user_feed_back_layout.*
import permissions.dispatcher.ktx.withPermissionsCheck
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

/**
 * 用户反馈
 */
class UserFeedBackActivity : BaseActivity<UserFeedBackViewModel>() {
    private val mPhotoMultipleItemList = mutableListOf<PhotoMultipleItem>()
    private val mPhotoList = arrayListOf<String>()
    private val mImgSaveDir by lazy {
        getExternalFilesDir("pic")?.absolutePath
            ?: Environment.getExternalStorageDirectory().absolutePath + "/xiaomai/pic"
    }
    private val mAdapter by lazy {
        FeedBackPhotoAdapter(mPhotoMultipleItemList)
    }

    override fun layoutId(): Int = R.layout.activity_user_feed_back_layout

    override fun initView(savedInstanceState: Bundle?) {
        photoRecyclerView.layoutManager = GridLayoutManager(this, 3)
        photoRecyclerView.addItemDecoration(SpacesItemDecoration(dp2px(10), 3))
        mPhotoMultipleItemList.add(PhotoMultipleItem())
        photoRecyclerView.adapter = mAdapter
        initObserver()
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        //添加照片
        mAdapter.mAddPhotoCallBack = {
            addPhoto()
        }
        mAdapter.mContentClickCallBack = {
            val intent = Intent(
                this,
                PreviewResultListActivity::class.java
            )
            intent.putStringArrayListExtra("imgUrls", mPhotoList)
            intent.putExtra("pos", mPhotoMultipleItemList.indexOf(it))
            startActivity(intent)
        }
        mAdapter.mDeleteContentCallBack = {
            val index = mPhotoMultipleItemList.indexOf(it)
            mPhotoMultipleItemList.remove(it)
            mPhotoList.removeAt(index)
            if (mPhotoMultipleItemList.size < 9) {
                val item = mPhotoMultipleItemList.find {
                    it.mPhotoPath == null
                }
                if (item == null) {
                    mPhotoMultipleItemList.add(PhotoMultipleItem())
                }
            }
            mAdapter.notifyDataSetChanged()
        }
        etInputProblem.textWatcher {
            afterTextChanged {
                val content = etInputProblem.text.toString().trim()
                tvInputProblemNums.text = buildString {
                    append(content.length)
                    append("/200")
                }
            }
        }
        tvSubmit.clickN {
            val content = etInputProblem.text.toString().trim()
            val phone = etInputPhone.text.toString().trim()
            if (content.isEmpty() || content.length < 5) {
                ToastUtils.toastShort("请至少输入5个字")
                return@clickN
            }
            if (!phone.isPhone()) {
                ToastUtils.toastShort("请输入正确的手机号")
                return@clickN
            }
            mViewModel.submitFeedBackData(content, phone, mPhotoList)
        }
    }

    private fun addPhoto() {
        withPermissionsCheck(Manifest.permission.WRITE_EXTERNAL_STORAGE, onShowRationale = {
            it.proceed()
        }) {
            ImagePicker.withMulti(CustomImgPickerPresenter())//指定presenter
                //设置选择的最大数
                .setMaxCount(9 - mPhotoList.size)
                //设置列数
                .setColumnCount(4)
                //设置要加载的文件类型，可指定单一类型
                .mimeTypes(MimeType.ofImage())
                .showCamera(true)//显示拍照
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
                        compressImg(filePathList)
                    }
                }
        }
    }

    /**
     * 压缩图片
     */
    private fun compressImg(imagePath: List<String>) {
        Luban.with(this)
            .load(imagePath)
            .ignoreBy(100)
            .setTargetDir(mImgSaveDir)
            .setCompressListener(object : OnCompressListener {
                override fun onSuccess(file: File?) {
                    if (file != null && file.exists()) {
                        mPhotoList.add(file.absolutePath)
                        mPhotoMultipleItemList.add(
                            mPhotoMultipleItemList.size - 1,
                            PhotoMultipleItem(file.absolutePath)
                        )
                        if (mPhotoList.size == 9) {
                            mPhotoMultipleItemList.removeAt(mPhotoMultipleItemList.size - 1)
                        }
                        mAdapter.notifyDataSetChanged()
                    }
                }

                override fun onError(e: Throwable?) {
                }

                override fun onStart() {
                }

            }).launch()
    }

    private fun initObserver() {
        mViewModel.mSubmitResultObserver.observe(this, Observer {
            if (it) {
                finish()
            }
        })
    }
}