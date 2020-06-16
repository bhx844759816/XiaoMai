package com.guangzhida.xiaomai.ui.chat

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.event.LiveDataBus
import com.guangzhida.xiaomai.event.LiveDataBusKey.PUBLISH_APPOINTMENT_FINISH_KEY
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.ui.chat.viewmodel.AppointmentPublishViewModel
import com.guangzhida.xiaomai.ui.home.adapter.PhotoMultipleItem
import com.guangzhida.xiaomai.ui.user.adapter.FeedBackPhotoAdapter
import com.guangzhida.xiaomai.view.SpacesItemDecoration
import com.guangzhida.xiaomai.view.custom.CustomImgPickerPresenter
import com.guangzhida.xiaomai.view.preview.PreviewResultListActivity
import com.ypx.imagepicker.ImagePicker
import com.ypx.imagepicker.bean.MimeType
import com.ypx.imagepicker.bean.SelectMode
import kotlinx.android.synthetic.main.activity_appointment_publish_layout.*
import permissions.dispatcher.ktx.withPermissionsCheck
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File


/**
 * 发布约吗页面
 */
class AppointmentPublishActivity : BaseActivity<AppointmentPublishViewModel>() {
    private val mPhotoMultipleItemList = mutableListOf<PhotoMultipleItem>()
    private val mPhotoList = arrayListOf<String>()
    private val mImgSaveDir by lazy {
        getExternalFilesDir("pic")?.absolutePath
            ?: Environment.getExternalStorageDirectory().absolutePath + "/xiaomai/pic"
    }
    private val mAdapter by lazy {
        FeedBackPhotoAdapter(mPhotoMultipleItemList)
    }


    override fun layoutId(): Int = R.layout.activity_appointment_publish_layout

    override fun initView(savedInstanceState: Bundle?) {
        photoRecyclerView.layoutManager = GridLayoutManager(this, 3)
        photoRecyclerView.addItemDecoration(SpacesItemDecoration(dp2px(10), 3))
        mPhotoMultipleItemList.add(PhotoMultipleItem())
        photoRecyclerView.adapter = mAdapter
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        rgSelectMoneyType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbSelectMoneyTypeOne -> {
                    etActivityMoney.gone()
                }
                R.id.rbSelectMoneyTypeTwo, R.id.rbSelectMoneyTypeThree, R.id.rbSelectMoneyTypeFour -> {
                    etActivityMoney.visible()
                }
            }
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
        //选择活动时间
        tvSelectActivityTime.clickN {
            showDateTimePickerDialog(tvSelectActivityTime)
        }
        //选择报名截止时间
        tvSignUpEndTime.clickN {
            showDateTimePickerDialog(tvSignUpEndTime)
        }

        tvPublish.clickN {
            doSubmit()
        }

        mViewModel.mSubmitResultObserver.observe(this, Observer {
            if (it) {
                LiveDataBus.with(PUBLISH_APPOINTMENT_FINISH_KEY).postValue(true)
                finish()
            }
        })
    }


    /**
     * 发布
     */
    private fun doSubmit() {
        val title = etTitle.text.toString().trim()
        val dec = etDec.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val money = etActivityMoney.text.toString().trim()
        val activityTime = tvSelectActivityTime.text.toString().trim()
        val signUpTime = tvSignUpEndTime.text.toString().trim()
        val boyPeoples = etBoyPeoples.text.toString().trim()
        val girlPeoples = etGirlPeoples.text.toString().trim()
        val moneyType = getMoneyType()
        if (title.isEmpty()) {
            toast("请输入标题")
            return
        }
        if (dec.isEmpty()) {
            toast("请输入描述")
            return
        }
        if (address.isEmpty()) {
            toast("请输入活动地址")
            return
        }
        if (moneyType > 0) {
            if (money.isEmpty()) {
                toast("请输入活动经费")
                return
            }
        }
        if (boyPeoples.isEmpty() && girlPeoples.isEmpty()) {
            toast("请至少输入一个参与人数")
            return
        }
        if (activityTime.isEmpty()) {
            toast("请选择活动开始时间")
            return
        }
        if (signUpTime.isEmpty()) {
            toast("请选择报名截止时间")
            return
        }
        if (mPhotoList.isEmpty()) {
            toast("请至少上传一张图片")
            return
        }
        mViewModel.doSubmit(
            title,
            dec,
            address,
            moneyType,
            money,
            activityTime,
            signUpTime,
            boyPeoples,
            girlPeoples,
            mPhotoList
        )
    }

    private fun getMoneyType(): Int {
        return when {
            rbSelectMoneyTypeOne.isChecked -> {
                return 0
            }
            rbSelectMoneyTypeTwo.isChecked -> {
                return 1
            }
            rbSelectMoneyTypeThree.isChecked -> {
                return 2
            }
            rbSelectMoneyTypeFour.isChecked -> {
                return 3
            }
            else -> {
                0
            }
        }
    }

    /**
     * 展示日期时间选择器
     */
    private fun showDateTimePickerDialog(tv: TextView) {
        TimePickerBuilder(this,
            OnTimeSelectListener { date, _ ->
                tv.text = date.time.formatDateTime("yyyy/MM/dd HH:mm")
            })
            .setType(BooleanArray(6) {
                it < 5
            })
            .setSubmitColor(Color.WHITE)//确定按钮文字颜色
            .setCancelColor(Color.WHITE)//取消按钮文字颜色
            .isCyclic(true)
            .build()
            .show(true)

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
}