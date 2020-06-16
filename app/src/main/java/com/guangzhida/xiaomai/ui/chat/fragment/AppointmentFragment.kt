package com.guangzhida.xiaomai.ui.chat.fragment

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fengchen.uistatus.UiStatusController
import com.fengchen.uistatus.annotation.UiStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.dialog.SelectSchoolDialog
import com.guangzhida.xiaomai.event.LiveDataBus
import com.guangzhida.xiaomai.event.LiveDataBusKey
import com.guangzhida.xiaomai.event.LiveDataBusKey.PUBLISH_APPOINTMENT_FINISH_KEY
import com.guangzhida.xiaomai.event.LiveDataBusKey.SCHOOL_MODEL_CHANGE_KEY
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.model.AppointmentModel
import com.guangzhida.xiaomai.model.SchoolModel
import com.guangzhida.xiaomai.ui.chat.AppointmentDetailsActivity
import com.guangzhida.xiaomai.ui.chat.AppointmentPublishActivity
import com.guangzhida.xiaomai.ui.chat.AppointmentUserDetailsActivity
import com.guangzhida.xiaomai.ui.chat.adapter.AppointmentAdapter
import com.guangzhida.xiaomai.ui.chat.viewmodel.AppointmentViewModel
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.Preference
import com.guangzhida.xiaomai.utils.ToastUtils
import com.guangzhida.xiaomai.view.RecyclerViewItemDivision
import kotlinx.android.synthetic.main.fragment_appointment_layout.*

/**
 * 约吗
 */
class AppointmentFragment : BaseFragment<AppointmentViewModel>() {
    private val mList = mutableListOf<AppointmentModel>()
    private val mAdapter by lazy {
        AppointmentAdapter(mList).apply {
            animationEnable = false
        }
    }
    private var distance = 0
    private var visible = true
    private lateinit var mUiStatusController: UiStatusController
    private var mSchoolModelList: List<SchoolModel>? = null
    private var mSchoolModel: SchoolModel? = null
    private var mSchoolInfoGson by Preference(Preference.SCHOOL_INFO_GSON, "")
    //选中的学校信息
    private var mSchoolSelectInfoGson by Preference(Preference.SCHOOL_SELECT_INFO_GSON, "")
    private val mGson by lazy {
        Gson()
    }

    override fun layoutId(): Int = R.layout.fragment_appointment_layout

    override fun initView(savedInstanceState: Bundle?) {
        mUiStatusController = UiStatusController.get().bind(recyclerView)
        mSchoolModelList = mGson.fromJson<List<SchoolModel>>(mSchoolInfoGson, object :
            TypeToken<List<SchoolModel>>() {
        }.type)
        mSchoolModel = mGson.fromJson<SchoolModel>(mSchoolSelectInfoGson, SchoolModel::class.java)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(RecyclerViewItemDivision(20, Color.parseColor("#f0f3f4")))
        recyclerView.adapter = mAdapter
    }

    override fun lazyLoadData() {
        if (BaseApplication.instance().mUserModel == null) {
            mUiStatusController.changeUiStatus(UiStatus.NOT_FOUND)
        } else {
            getData(true)
        }
    }

    override fun initListener() {
        mAdapter.mItemClickCallBack = {
            if (it.userId.toString() == BaseApplication.instance().mUserModel?.id) {
                startKtxActivity<AppointmentUserDetailsActivity>(
                    value = Pair(
                        "appointmentModel",
                        mGson.toJson(it)
                    )
                )
            } else {
                startKtxActivity<AppointmentDetailsActivity>(
                    value = Pair(
                        "appointmentModel",
                        mGson.toJson(it)
                    )
                )
            }

        }
        mAdapter.mSignUpClickCallBack = {
            if (mSchoolModel != null) {
                viewModel.singUpActivity(it.id.toString(), mSchoolModel!!.id)
            }
        }
        ivAddAppointment.clickN {
            if (BaseApplication.instance().mUserModel != null) {
                if (mSchoolModel != null) {
                    startKtxActivity<AppointmentPublishActivity>()
                } else {
                    ToastUtils.toastShort("选择完学校才可以发布")
                    showSelectSchoolDialog()
                }
            } else {
                ToastUtils.toastShort("登录后才可以发布")
            }
        }
        smartRefreshLayout.setOnRefreshListener {
            getData(true)
        }
        smartRefreshLayout.setOnLoadMoreListener {
            getData(false)
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (distance < -ViewConfiguration.getTouchSlop() && !visible) {
                    showAnim()
                    distance = 0;
                    visible = true;
                } else if (distance > ViewConfiguration.getTouchSlop() && visible) {
                    hideAnim()
                    distance = 0;
                    visible = false;
                }
                if ((dy > 0 && visible) || (dy < 0 && !visible))//向下滑并且可见  或者  向上滑并且不可见
                    distance += dy
            }
        })
        viewModel.mRefreshObserver.observe(this, Observer {
            smartRefreshLayout.finishRefresh()
            if (!it) {
                if (mList.isEmpty()) {
                    mUiStatusController.changeUiStatus(UiStatus.NETWORK_ERROR)
                } else {
                    smartRefreshLayout.finishLoadMore(it)
                }
            }
        })
        viewModel.mSchoolModelListData.observe(this, Observer {
            mSchoolInfoGson = mGson.toJson(it)
            mSchoolModelList = it
            showSelectSchoolDialog()
        })
        viewModel.mResultListObserver.observe(this, Observer {
            if (it.second.isNotEmpty()) {
                mUiStatusController.changeUiStatus(UiStatus.CONTENT)
                if (it.first) {
                    mList.clear()
                    mList.addAll(it.second)
                } else {
                    mList.addAll(it.second)
                }
                mAdapter.notifyDataSetChanged()
                smartRefreshLayout.finishLoadMore(true)
            } else {
                if (it.first) {
                    mList.clear()
                    mAdapter.notifyDataSetChanged()
                    mUiStatusController.changeUiStatus(UiStatus.EMPTY)
                } else {
                    smartRefreshLayout.finishLoadMoreWithNoMoreData()
                }
            }
        })


        //用户状态改变的时候
        userModelChangeLiveData.observe(this, Observer {
            if (BaseApplication.instance().mUserModel != null) {
                mUiStatusController.changeUiStatus(UiStatus.LOADING)
                mList.clear()
                mAdapter.notifyDataSetChanged()
                getData(true)
            } else {
                mUiStatusController.changeUiStatus(UiStatus.NOT_FOUND)
            }
        })
        //当发布成功的时候
        LiveDataBus.with(PUBLISH_APPOINTMENT_FINISH_KEY, Boolean::class.java)
            .observe(this, Observer {
                getData(true)
            })
        //选择的学校改变的
        LiveDataBus.with(LiveDataBusKey.SCHOOL_MODEL_CHANGE_KEY, SchoolModel::class.java)
            .observe(this, Observer {
                mSchoolModel = it
                getData(true)
            })
    }

    private fun getData(isRefresh: Boolean) {
        if (mSchoolModel == null) {
            ToastUtils.toastShort("请先选择学校")
            showSelectSchoolDialog()
        } else {
            viewModel.getData(isRefresh, mSchoolModel!!.id)
        }
    }

    private fun hideAnim() {
        ivAddAppointment
            .animate()
            .translationY(ivAddAppointment.height * 2.0f).apply {
                interpolator = AccelerateInterpolator(2f)
                duration = 300
            }.start()
    }

    private fun showAnim() {
        ivAddAppointment
            .animate()
            .translationY(0f).apply {
                interpolator = DecelerateInterpolator(2f)
                duration = 300
            }.start()
    }

    /**
     * 弹出选择学校的Dialog
     */
    private fun showSelectSchoolDialog() {
        activity?.let {
            if (mSchoolModelList.isNullOrEmpty()) {
                viewModel.getAllSchoolInfo()
            } else {
                val items = mSchoolModelList!!.map { schoolModel ->
                    schoolModel.name
                }
                SelectSchoolDialog.showDialog(it, it, items) { index ->
                    mSchoolModel = mSchoolModelList!![index]
                    mSchoolSelectInfoGson = Gson().toJson(mSchoolModel)
                    //选择学校改变的时候发送通知
                    LiveDataBus.with(SCHOOL_MODEL_CHANGE_KEY).postValue(mSchoolModel)
                }
            }
        }
    }
}