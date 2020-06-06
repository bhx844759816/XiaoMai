package com.guangzhida.xiaomai.ui.chat.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.flyco.tablayout.listener.CustomTabEntity
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.ktxlibrary.ext.dp2px
import com.guangzhida.xiaomai.ktxlibrary.ext.gone
import com.guangzhida.xiaomai.ktxlibrary.ext.visible
import com.guangzhida.xiaomai.model.TabEntity
import com.guangzhida.xiaomai.ui.chat.viewmodel.InteractionViewModel
import kotlinx.android.synthetic.main.fragment_interaction_layout.*
import kotlinx.android.synthetic.main.fragment_interaction_layout.tabLayout
import kotlinx.android.synthetic.main.fragment_message.*
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
import java.util.ArrayList

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
    private var mBadgeTextView: View? = null
    private var mOld = 0
    private var isShowBadgeView = false

    override fun layoutId(): Int = R.layout.fragment_interaction_layout

    override fun initView(savedInstanceState: Bundle?) {
        mTabEntities.add(TabEntity(mTitles[0]))
        mTabEntities.add(TabEntity(mTitles[1]))
        viewPager.adapter = MyFragmentPageAdapter()
        mFragmentContainerHelper.handlePageSelected(0, false)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

            }
        })
        initTabLayout()
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
                if (index == 1) {
                    mBadgeTextView = LayoutInflater.from(context)
                        .inflate(R.layout.simple_red_dot_badge_layout, null);
                    badgePagerTitleView.badgeView = mBadgeTextView;
                    badgePagerTitleView.xBadgeRule = BadgeRule(BadgeAnchor.CONTENT_RIGHT, 0)
                    badgePagerTitleView.yBadgeRule = BadgeRule(BadgeAnchor.CONTENT_TOP, 0)
                    badgePagerTitleView.isAutoCancelBadge = false
                    if (isShowBadgeView) mBadgeTextView?.visible() else mBadgeTextView?.gone()
                }
                return badgePagerTitleView
            }

            override fun getCount(): Int = mTitles.size

            override fun getIndicator(context: Context?): IPagerIndicator {
                val indicator = LinePagerIndicator(context);
                indicator.setColors(Color.parseColor("#9245ec"))
                return indicator;
            }
        }
        tabLayout.navigator = commonNavigator
        val titleContainer = commonNavigator.titleContainer; // must after setNavigator
        titleContainer.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        titleContainer.dividerPadding = context?.dp2px(15) ?: 0
        titleContainer.dividerDrawable = resources.getDrawable(R.drawable.simple_splitter)
        mFragmentContainerHelper.attachMagicIndicator(tabLayout)
    }

    private fun switchPage(index: Int) {
        viewPager.currentItem = index
    }

    inner class MyFragmentPageAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = mFragments.size

        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }
    }
}