package com.guangzhida.xiaomai.ui.chat.adapter

import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadCircleImage
import com.guangzhida.xiaomai.ext.loadFilletRectangle
import com.guangzhida.xiaomai.http.BASE_URL
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.room.entity.UserEntity
import com.guangzhida.xiaomai.view.azlist.AZItemEntity

/**
 * 联系人列表
 *
 */
class ContactListAdapter2(list: MutableList<AZItemEntity<UserEntity>>) :
    BaseQuickAdapter<AZItemEntity<UserEntity>, BaseViewHolder>(
        R.layout.adapter_contact_list_layout, list
    ) {
    private val mDataList = list
    var mContentClickCallBack: ((UserEntity) -> Unit)? = null
    override fun convert(helper: BaseViewHolder, item: AZItemEntity<UserEntity>) {
        val parent = helper.getView<ConstraintLayout>(R.id.parent)
        val ivHeaderView = helper.getView<ImageView>(R.id.ivHeaderView)
        ivHeaderView.loadFilletRectangle(
            BASE_URL.substring(0, BASE_URL.length - 1) + item.value.avatarUrl
        )
        //设置备注或者昵称
        helper.setText(
            R.id.tvName, if (item.value.remarkName.isNotEmpty()) {
                item.value.remarkName
            } else {
                item.value.nickName
            }
        )
        parent.setOnClickListener {
            mContentClickCallBack?.invoke(item.value)
        }


    }


    fun getSortLetters(position: Int): String? {
        return if (mDataList.isEmpty()) {
            null
        } else mDataList[position - headerLayoutCount].sortLetters
    }

    /**
     * 获取当前首字母
     */
    fun getSortLettersFirstPosition(letters: String): Int {
        if (mDataList.isEmpty()) {
            return -1
        }
        var position = -1
        for (index in mDataList.indices) {
            if (mDataList[index].sortLetters == letters) {
                position = index
                break
            }
        }
        return position
    }

    /**
     * 获取下一个
     */
    fun getNextSortLetterPosition(position: Int): Int {
        val relPosition = position - headerLayoutCount
        if (mDataList.isEmpty() || mDataList.size <= relPosition + 1) {
            return -1
        }
        var resultPosition = -1
        for (index in relPosition + 1 until mDataList.size) {
            if (mDataList[relPosition].sortLetters != mDataList[index].sortLetters) {
                resultPosition = index
                break
            }
        }
        return resultPosition
    }

}