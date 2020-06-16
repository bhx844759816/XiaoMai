package com.guangzhida.xiaomai.ui.chat.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ext.loadImage
import com.guangzhida.xiaomai.http.BASE_URL
import com.youth.banner.adapter.BannerAdapter

class ImageBannerAdapter(data: MutableList<String>) :
    BannerAdapter<String, ImageBannerAdapter.BannerViewHolder>(data) {

    inner class BannerViewHolder constructor(var imageView: ImageView) :
        RecyclerView.ViewHolder(imageView) {

    }

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): BannerViewHolder {
        val imageView = ImageView(parent?.context);
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        return BannerViewHolder(imageView)
    }


    override fun onBindView(holder: BannerViewHolder?, data: String?, position: Int, size: Int) {
        data?.let {
            holder?.imageView?.loadImage(
                BASE_URL + it,
                0,
                R.mipmap.icon_img_error_holder_h
            )
        }
    }
}