package com.guangzhida.xiaomai.ui.user.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadImage
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.model.PackageInfoModel
import com.guangzhida.xiaomai.ui.chat.adapter.ChatMultipleItem
import com.guangzhida.xiaomai.ui.home.adapter.PhotoMultipleItem

class FeedBackPhotoAdapter(list: MutableList<PhotoMultipleItem>) :
    BaseMultiItemQuickAdapter<PhotoMultipleItem, BaseViewHolder>(list) {

    var mAddPhotoCallBack: (() -> Unit)? = null //点击添加图片
    var mContentClickCallBack: ((PhotoMultipleItem) -> Unit)? = null //点击内容
    var mDeleteContentCallBack: ((PhotoMultipleItem) -> Unit)? = null //删除图片

    init {
        addItemType(
            PhotoMultipleItem.ADD_PHOTO,
            R.layout.adapter_feed_back_photo_add_layout
        )//添加图片的
        addItemType(
            PhotoMultipleItem.CONTENT_PHOTO,
            R.layout.adapter_feed_back_photo_content_layout
        )//图片内容的
    }

    override fun convert(helper: BaseViewHolder, item: PhotoMultipleItem) {
        when (helper.itemViewType) {
            PhotoMultipleItem.ADD_PHOTO -> {
                helper.getView<ImageView>(R.id.ivAddPhoto).clickN {
                    mAddPhotoCallBack?.invoke()
                }
            }
            PhotoMultipleItem.CONTENT_PHOTO -> {
                val contentImageView = helper.getView<ImageView>(R.id.ivContent)
                contentImageView.loadImage(item.mPhotoPath)
                helper.getView<ImageView>(R.id.ivDelete).clickN {
                    mDeleteContentCallBack?.invoke(item)
                }
                contentImageView.clickN {
                    mContentClickCallBack?.invoke(item)
                }
            }
        }
    }
}