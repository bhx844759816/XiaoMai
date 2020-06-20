package com.guangzhida.xiaomai.ui.chat.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import com.flyco.tablayout.listener.CustomTabEntity
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.base.BaseFragment
import com.guangzhida.xiaomai.ktxlibrary.ext.dp2px
import com.guangzhida.xiaomai.ktxlibrary.ext.gone
import com.guangzhida.xiaomai.ktxlibrary.ext.visible
import com.guangzhida.xiaomai.model.TabEntity
import com.guangzhida.xiaomai.ui.chat.AddFriendsActivity
import com.guangzhida.xiaomai.ui.chat.viewmodel.ChatViewModel
import com.guangzhida.xiaomai.utils.LogUtils
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
import java.util.*

/**
 * 互动的fragment
 */
class ChatFragment : BaseFragment<ChatViewModel>() {
    private val mTitles = arrayOf("消息", "联系人")
    private val mFragments = listOf(
        ConversationFragment(),
        ContactListFragment()
    )
    private val mTabEntities = ArrayList<CustomTabEntity>()
    private val mFragmentContainerHelper = FragmentContainerHelper()
    private var mBadgeTextView: View? = null
    private var mOld = 0
    private var isShowBadgeView = false
    override fun layoutId(): Int = R.layout.fragment_message

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mTabEntities.add(TabEntity(mTitles[0]))
        mTabEntities.add(TabEntity(mTitles[1]))
        childFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, mFragments[0])
            .commitNow()
        mFragmentContainerHelper.handlePageSelected(0, false)
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
                    if(index != mOld){
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


    override fun initListener() {
        ivAddFriend.setOnClickListener {
            startActivity(Intent(context, AddFriendsActivity::class.java))
        }
    }

    private fun switchPage(index: Int) {
        val now = mFragments[index]
        childFragmentManager.beginTransaction().apply {
            if (!now.isAdded) {
                add(R.id.fragment, now)
            }
            show(now)
            hide(mFragments[mOld])
            commit()
            mOld = index
        }
    }

    fun selectDefault() {
        LogUtils.i("lifecycle.currentState =${lifecycle.currentState}")
        if (lifecycle.currentState == Lifecycle.State.STARTED && mOld != 0) {
            switchPage(0)
            mFragmentContainerHelper.handlePageSelected(0, false)
        }
    }

    /**
     * 展示小红点
     */
    fun showBadgeView() {
        isShowBadgeView = true
        if (lifecycle.currentState >= Lifecycle.State.STARTED) {
            mBadgeTextView?.visible()
        }
    }

    fun hideBadgeView() {
        isShowBadgeView = false
        if (lifecycle.currentState >= Lifecycle.State.CREATED) {
            mBadgeTextView?.gone()
        }
    }

}