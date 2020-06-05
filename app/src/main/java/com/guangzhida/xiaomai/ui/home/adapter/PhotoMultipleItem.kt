package com.guangzhida.xiaomai.ui.home.adapter

import com.chad.library.adapter.base.entity.MultiItemEntity

class PhotoMultipleItem(photoPath: String? = null) : MultiItemEntity {
    val mPhotoPath = photoPath
    override val itemType: Int
        get() = if (mPhotoPath.isNullOrEmpty()) {
            ADD_PHOTO
        } else {
            CONTENT_PHOTO
        }


    companion object {
        const val ADD_PHOTO = 0 //左边 - 文本消息
        const val CONTENT_PHOTO = 1 //左边 - 文本消息
    }


}