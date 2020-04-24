package com.guangzhida.xiaomai.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.ashokvarma.bottomnavigation.BottomNavigationBar
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.EaseUiHelper
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseActivity
import com.guangzhida.xiaomai.event.messageCountChangeLiveData
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
        viewPager.adapter = MyFragmentPageAdapter()
        viewPager.setPageTransformer { page, position ->
            LogUtils.i("position=$position")
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                id_bottomNavigationBar.selectTab(position)
            }
        })
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
                        ToastUtils.toastShort("未登录请先登录")
                        startKtxActivity<LoginActivity>()
                        id_bottomNavigationBar.selectTab(mOldPos, false)
                    } else {
                        changeTab(position)
                    }
                } else {
                    changeTab(position)
                }
            }
        })
        messageCountChangeLiveData.observe(this, Observer {
            id_bottomNavigationBar.setUnReadMessageCount(EaseUiHelper.getUnReadMessageCount())
        })
    }

    override fun onResume() {
        super.onResume()
        //更新小红点
        id_bottomNavigationBar.setUnReadMessageCount(EaseUiHelper.getUnReadMessageCount())
    }

    private fun changeTab(pos: Int) {
        viewPager.currentItem = pos
        if(pos == 1){
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