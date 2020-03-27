package com.guangzhida.xiaomai.model

import com.flyco.tablayout.listener.CustomTabEntity

class TabEntity(title: String, tabSelectedIcon: Int = 0, tabUnSelectIcon: Int = 0) : CustomTabEntity {
    private val mTitle = title
    private val mTabSelectedIcon = tabSelectedIcon
    private val mTabUnSelectIcon = tabUnSelectIcon
    override fun getTabUnselectedIcon(): Int = mTabUnSelectIcon

    override fun getTabSelectedIcon(): Int = mTabSelectedIcon

    override fun getTabTitle(): String {
        return mTitle
    }
}