package com.guangzhida.xiaomai.ui.chat.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.flyco.tablayout.listener.CustomTabEntity
import com.guangzhida.xiaomai.BaseApplication
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.ktxlibrary.ext.*
import com.guangzhida.xiaomai.model.TabEntity
import com.guangzhida.xiaomai.ui.chat.ContactListActivity
import com.guangzhida.xiaomai.ui.chat.viewmodel.InteractionViewModel
import com.guangzhida.xiaomai.utils.ToastUtils
import kotlinx.android.synthetic.main.fragment_interaction_layout.*
import net.lucode.hackware.magicindicator.FragmentContainerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeAnchor
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgePagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeRule
import java.util.*

/**
 * 互动的Fragment
 *  包含 消息 约吗  联系人
 */
class InteractionFragment : BaseFragment<InteractionViewModel>() {

    private val mTitles = arrayOf("消息", "约吗")
    private val mTabEntities = ArrayList<CustomTabEntity>()
    private val mFragments = listOf(
        ConversationFragment(), //消息
        AppointmentFragment() //约吗
    )
    private val mFragmentContainerHelper = FragmentContainerHelper()
    private var mOld = 0
    private var isShowBadgeView = false

    override fun layoutId(): Int = R.layout.fragment_interaction_layout

    override fun initView(savedInstanceState: Bundle?) {
        mTabEntities.add(TabEntity(mTitles[0]))
        mTabEntities.add(TabEntity(mTitles[1]))
        viewPager.adapter = MyFragmentPageAdapter2()
        mFragmentContainerHelper.handlePageSelected(0, false)
        initTabLayout()

    }

    override fun initListener() {
        viewPager.offscreenPageLimit = 2
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                tabLayout.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                tabLayout.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                tabLayout.onPageSelected(position)
                mOld = position
            }

        })

        //跳转到联系人
        ivUserContact.clickN {
            if (BaseApplication.instance().mUserModel != null) {
                startKtxActivity<ContactListActivity>()
            } else {
                ToastUtils.toastShort("登录后才可以查看")
            }
        }
    }

    /**
     * 初始化TabLayout
     */
    private fun initTabLayout() {
        val commonNavigator = CommonNavigator(context);
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
                val badgePagerTitleView = BadgePagerTitleView(context)
                val simplePagerTitleView = ColorTransitionPagerTitleView(context)
                simplePagerTitleView.text = mTitles[index]
                simplePagerTitleView.normalColor = Color.parseColor("#999999")
                simplePagerTitleView.selectedColor = Color.parseColor("#282828")
                simplePagerTitleView.setOnClickListener {
                    if (index != mOld) {
                        mFragmentContainerHelper.handlePageSelected(index, false)
                        switchPage(index)
                    }
                }
                badgePagerTitleView.innerPagerTitleView = simplePagerTitleView
                return badgePagerTitleView
            }

            override fun getCount(): Int = mTitles.size

            override fun getIndicator(context: Context?): IPagerIndicator {
                val indicator = LinePagerIndicator(context);
                indicator.mode = LinePagerIndicator.MODE_EXACTLY;
                indicator.lineHeight = context?.dp2px(4)?.toFloat() ?: 0f
                indicator.lineWidth = context?.dp2px(40)?.toFloat() ?: 0f
                indicator.roundRadius = context?.dp2px(2)?.toFloat() ?: 0f
                indicator.yOffset = context?.dp2px(6)?.toFloat() ?: 0f
                indicator.startInterpolator = AccelerateInterpolator();
                indicator.endInterpolator = DecelerateInterpolator(2.0f);
                indicator.setColors(Color.parseColor("#9245ec"))
                return indicator;
            }
        }
        tabLayout.navigator = commonNavigator
        mFragmentContainerHelper.attachMagicIndicator(tabLayout)
    }

    private fun switchPage(index: Int) {
        viewPager.currentItem = index
        mOld = index
    }

    inner class MyFragmentPageAdapter2 :
        FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment = mFragments[position]

        override fun getCount(): Int = mFragments.size


    }


    /**
     * 展示小红点
     */
    fun showBadgeView() {
        if (lifecycle.currentState == Lifecycle.State.STARTED) {
            ivUserContactMessageTips.visible()
        }
    }

    /**
     * 隐藏小红点
     */
    fun hideBadgeView() {
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            ivUserContactMessageTips.gone()
        }
    }
}