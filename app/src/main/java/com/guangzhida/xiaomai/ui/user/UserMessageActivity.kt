package com.guangzhida.xiaomai.ui.user

import android.Manifest
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.dialog.SelectPhotoDialog
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ui.user.viewmodel.UserMessageModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.view.custom.CustomImgPickerPresenter
import com.ypx.imagepicker.ImagePicker
import com.ypx.imagepicker.bean.MimeType
import com.ypx.imagepicker.bean.SelectMode
import com.ypx.imagepicker.bean.selectconfig.CropConfig
import kotlinx.android.synthetic.main.activity_user_message_layout.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import permissions.dispatcher.ktx.withPermissionsCheck
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

/**
 * 个人信息
 */
@RuntimePermissions
class UserMessageActivity : BaseActivity<UserMessageModel>() {
    private var mPhotoFile: File? = null
    private val mParams = mutableMapOf<String, String>()
    override fun layoutId(): Int = R.layout.activity_user_message_layout
    private val mImgSaveDir by lazy {
        getExternalFilesDir("pic")?.absolutePath
            ?: Environment.getExternalStorageDirectory().absolutePath + "/xiaomai/pic"
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        BaseApplication.instance().mUserModel?.let {
            LogUtils.i("user=$it")
            ivUserAvatar.loadCircleImage(
                BASE_URL.substring(
                    0,
                    BASE_URL.length - 1
                ) + it.headUrl,
                holder = R.mipmap.icon_default_header
            )
            etUserNickName.setText(it.name)
            tvUserId.text = it.id
            if (it.sex == 1) {
                rbSexBoy.isChecked = true
                rbSexGirl.isChecked = false
            } else if (it.sex == 2) {
                rbSexBoy.isChecked = false
                rbSexGirl.isChecked = true
            }
            etUserAge.setText(buildString {
                append(it.age)
            })
            etUserSignUp.setText(it.signature ?: "")
        }
        //用户信息修改结果
        mViewModel.modifyUserMessageLiveData.observe(this, Observer {
            if (it) {
                ToastUtils.toastShort("修改信息成功")
                finish()
            }
        })
    }

    override fun initListener() {
        rlSave.setOnClickListener {
            val nickName = etUserNickName.text.toString().trim()
            val age = etUserAge.text.toString().trim()
            val signUp = etUserSignUp.text.toString().trim() //个性签名
            if (nickName.isEmpty()) {
                ToastUtils.toastShort("请输入昵称")
                return@setOnClickListener
            }
            if (age.isEmpty()) {
                ToastUtils.toastShort("请输入年龄")
                return@setOnClickListener
            }
            if (rbSexBoy.isChecked) {
                mParams["sex"] = "1"
            } else if (rbSexGirl.isChecked) {
                mParams["sex"] = "2"
            }
            mParams["age"] = age
            mParams["nickName"] = nickName
            mParams["signature"] = signUp
            mViewModel.uploadMessage(mPhotoFile, mParams)
        }
        ivUserAvatar.setOnClickListener {
            SelectPhotoDialog.showDialog(this, this) { index ->
                if (index == 0) {
                    //拍照
                    takePhoto()
                } else if (index == 1) {
                    //选择图片从相册
                    selectPhoto()
                }
            }
        }
        //返回
        toolBar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * 拍照
     */
    private fun takePhoto() = withPermissionsCheck(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        onShowRationale = {
            it.proceed()
        }) {
        val cropConfig = CropConfig().apply {
            saveInDCIM(false)
            setCropRatio(1, 1)
            cropRectMargin = 100
            isCircle = true
            cropStyle = CropConfig.STYLE_FILL
            cropGapBackgroundColor = Color.TRANSPARENT
        }
        ImagePicker.takePhotoAndCrop(this, CustomImgPickerPresenter(), cropConfig) {
            compressImg(it[0].path)
        };
    }


    /**
     * 选择图片
     */
    private fun selectPhoto() =
        withPermissionsCheck(Manifest.permission.WRITE_EXTERNAL_STORAGE, onShowRationale = {
            it.proceed()
        }) {
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
                .cropSaveInDCIM(false)
                .cropRectMinMargin(100)
                .cropStyle(CropConfig.STYLE_FILL)
                .cropGapBackgroundColor(Color.TRANSPARENT)
                .setCropRatio(1, 1)
                .cropAsCircle()
                .crop(this) {
                    compressImg(it[0].path)
                }
        }


    /**
     * 压缩图片
     */
    private fun compressImg(imagePath: String) {
        Luban.with(this)
            .load(imagePath)
            .ignoreBy(100)
            .setTargetDir(mImgSaveDir)
            .setCompressListener(object : OnCompressListener {
                override fun onSuccess(file: File?) {
                    if (file != null && file.exists()) {
                        mPhotoFile = file
                        ivUserAvatar.loadCircleImage(
                            file,
                            holder = R.mipmap.icon_default_header
                        )
                    }
                }

                override fun onError(e: Throwable?) {
                }

                override fun onStart() {
                }

            }).launch()
    }

}