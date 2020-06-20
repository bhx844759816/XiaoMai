package com.guangzhida.xiaomai.ui.appointment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.event.LiveDataBus
import com.guangzhida.xiaomai.event.LiveDataBusKey
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ui.appointment.fragment.AppointmentPublishCarFragment
import com.guangzhida.xiaomai.ui.appointment.fragment.AppointmentPublishPlayFragment
import com.guangzhida.xiaomai.ui.appointment.fragment.AppointmentPublishWorkFragment
import com.guangzhida.xiaomai.ui.appointment.viewmodel.AppointmentPublishViewModel
import kotlinx.android.synthetic.main.activity_appointment_publish_layout2.*

class AppointmentPublishActivity : BaseActivity<AppointmentPublishViewModel>() {
    private var mPublishCarFragment: AppointmentPublishCarFragment? = null
    private var mPublishPlayFragment: AppointmentPublishPlayFragment? = null
    private var mPublishWorkFragment: AppointmentPublishWorkFragment? = null
    private var mCurrentFragment: Fragment? = null

    override fun layoutId(): Int = R.layout.activity_appointment_publish_layout2

    override fun initView(savedInstanceState: Bundle?) {
        showAppointmentPlayFragment()
    }

    override fun initListener() {
        toolBar.setNavigationOnClickListener {
            finish()
        }
        rgSelectType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbSelectTypeOne -> {
                    showAppointmentPlayFragment()
                }
                R.id.rbSelectTypeTwo -> {
                    showAppointmentWorkFragment()
                }
                R.id.rbSelectTypeThree -> {
                    showAppointmentCarFragment()
                }
            }
        }
        tvPublish.clickN {
            publishAppointment()
        }
        mViewModel.mSubmitResultObserver.observe(this, Observer {
            if (it) {
                LiveDataBus.with(LiveDataBusKey.PUBLISH_APPOINTMENT_FINISH_KEY).postValue(true)
                finish()
            }
        })
    }

    private fun  publishAppointment(){
        mCurrentFragment?.let {
            when(it){
                is AppointmentPublishPlayFragment->{
                    it.publish()
                }
                is AppointmentPublishWorkFragment->{
                    it.publish()
                }
                is AppointmentPublishCarFragment->{
                    it.publish()
                }
            }
        }
    }

    private fun showAppointmentPlayFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        if (mPublishPlayFragment == null) {
            mPublishPlayFragment = AppointmentPublishPlayFragment()
        }
        hideAllFragment(transaction)
        if (!mPublishPlayFragment!!.isAdded) {
            transaction.add(R.id.fragment, mPublishPlayFragment!!)
        } else {
            transaction.show(mPublishPlayFragment!!)
        }
        mCurrentFragment = mPublishPlayFragment
        transaction.commit()
    }

    private fun showAppointmentCarFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        if (mPublishCarFragment == null) {
            mPublishCarFragment = AppointmentPublishCarFragment()
        }
        hideAllFragment(transaction)
        if (!mPublishCarFragment!!.isAdded) {
            transaction.add(R.id.fragment, mPublishCarFragment!!)
        } else {
            transaction.show(mPublishCarFragment!!)
        }
        mCurrentFragment = mPublishCarFragment
        transaction.commit()
    }

    private fun showAppointmentWorkFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        if (mPublishWorkFragment == null) {
            mPublishWorkFragment = AppointmentPublishWorkFragment()
        }
        hideAllFragment(transaction)
        if (!mPublishWorkFragment!!.isAdded) {
            transaction.add(R.id.fragment, mPublishWorkFragment!!)
        } else {
            transaction.show(mPublishWorkFragment!!)
        }
        mCurrentFragment = mPublishWorkFragment
        transaction.commit()
    }

    private fun hideAllFragment(transaction: FragmentTransaction) {
        mPublishCarFragment?.let {
            if (it.isResumed) {
                transaction.hide(it)
            }
        }
        mPublishPlayFragment?.let {
            if (it.isResumed) {
                transaction.hide(it)
            }
        }
        mPublishWorkFragment?.let {
            if (it.isResumed) {
                transaction.hide(it)
            }
        }

    }
}