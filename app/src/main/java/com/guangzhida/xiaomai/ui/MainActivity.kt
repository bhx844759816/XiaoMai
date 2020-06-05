package com.guangzhida.xiaomai.ui

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.chat.ChatHelper
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.dialog.SchoolPhoneAccountLoginDialog
import com.guangzhida.xiaomai.event.messageCountChangeLiveData
import com.guangzhida.xiaomai.event.userModelChangeLiveData
import com.guangzhida.xiaomai.ext.jumpLoginByState
import com.guangzhida.xiaomai.ktxlibrary.ext.permission.request
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.receiver.WifiStateManager
import com.guangzhida.xiaomai.ui.chat.fragment.ChatFragment
import com.guangzhida.xiaomai.ui.home.HomeFragment
import com.guangzhida.xiaomai.ui.login.LoginActivity
import com.guangzhida.xiaomai.utils.LogUtils
import com.guangzhida.xiaomai.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 首页面
 */
class MainActivity : BaseActivity<MainViewModel>() {
    private val mFragments = listOf(
        HomeFragment(),
        ChatFragment()
    )
    private var mOldPos = 0
    private var exitTime: Long = 0

    override fun initView(savedInstanceState: Bundle?) {
//        checkPermission()
        viewPager.adapter = MyFragmentPageAdapter()
        viewPager.isUserInputEnabled = BaseApplication.instance().mUserModel != null
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                id_bottomNavigationBar.selectTab(position)
            }
        })
    }

    private fun checkPermission() {
        request(Manifest.permission.ACCESS_FINE_LOCATION) {
            onShowRationale {
                it.retry()
            }
            onGranted {
                viewPager.adapter = MyFragmentPageAdapter()
            }
        }
    }

    override fun initListener() {
        id_bottomNavigationBar.setTabSelectedListener(object :
            BottomNavigationBar.OnTabSelectedListener {
            override fun onTabReselected(position: Int) {
            }

            override fun onTabUnselected(position: Int) {
            }

            override fun onTabSelected(position: Int) {
                if (position == 1) {
                    if (BaseApplication.instance().mUserModel == null) {
                        jumpLoginByState()
                        id_bottomNavigationBar.selectTab(mOldPos, false)
                    } else {
                        changeTab(position)
                    }
                } else {
                    changeTab(position)
                }
            }
        })
        userModelChangeLiveData.observe(this, Observer {
            viewPager.isUserInputEnabled = BaseApplication.instance().mUserModel != null
        })
        messageCountChangeLiveData.observe(this, Observer {
            if (BaseApplication.instance().mUserModel != null) {
                mViewModel.getFriendInvite()
            }
        })
        //有好友请求过来
        mViewModel.mFriendInviteCount.observe(this, Observer {
            LogUtils.i("mFriendInviteCount=$it")
            if (it > 0) {
                (mFragments[1] as ChatFragment).showBadgeView()
            } else {
                (mFragments[1] as ChatFragment).hideBadgeView()
            }
            id_bottomNavigationBar.setUnReadMessageCount(it + ChatHelper.getUnReadMessageCount())
        })


    }

    /**
     *
     * 检索是否有好友请求，有的话设置小红点 小红点数加一
     *
     */

    override fun onResume() {
        super.onResume()
        if (BaseApplication.instance().mUserModel != null) {
            mViewModel.getFriendInvite()
        }
    }

    private fun changeTab(pos: Int) {
        viewPager.currentItem = pos
        if (pos == 1) {
            (mFragments[pos] as ChatFragment).selectDefault()
        }
        mOldPos = pos
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        WifiStateManager.getInstance().registerNetReceiver(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        WifiStateManager.getInstance().unRegisterNetReceiver(this)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            ToastUtils.toastShort("再按一次退出程序")
            exitTime = System.currentTimeMillis()
        } else {
            moveTaskToBack(false)
        }
    }

    override fun layoutId(): Int = R.layout.activity_main

    inner class MyFragmentPageAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = mFragments.size

        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }
    }

}