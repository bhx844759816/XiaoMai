package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.ktxlibrary.ext.clickN
import com.guangzhida.xiaomai.ktxlibrary.ext.startKtxActivity
import com.guangzhida.xiaomai.model.PopAdModel
import com.guangzhida.xiaomai.ui.WebActivity

/**
 *
 */
object AppPopAdDialog {
    fun showDialog(context: Context, owner: LifecycleOwner, adModel: PopAdModel) {
        val layoutRes = when (adModel.type) {
            0, 1 -> {
                R.layout.dialog_app_pop_ad_content_layout
            }
            2 -> {
                R.layout.dialog_app_pop_ad_img_layout
            }
            else -> R.layout.dialog_app_pop_ad_content_layout
        }

        MaterialDialog(context)
            .cancelable(false)
            .cornerRadius(8f)
            .customView(
                viewRes = layoutRes,
                dialogWrapContent = true,
                noVerticalPadding = true,
                horizontalPadding = true
            )
            .show {
                val view = getCustomView()
                if (adModel.type == 0 || adModel.type == 1) {
                    val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
                    val tvContent = view.findViewById<TextView>(R.id.tvContent)
                    val tvConfirm = view.findViewById<TextView>(R.id.tvConfirm)
                    tvTitle.text = adModel.title
                    tvContent.text = adModel.content
                    tvConfirm.clickN {
                        if (adModel.link != null && adModel.link.isNotEmpty()) {
                            context.startKtxActivity<WebActivity>(
                                values = listOf(
                                    Pair("url", adModel.link),
                                    Pair("type", "ad")
                                )
                            )
                        }
                        this.dismiss()
                    }
                } else {
                    this.dialogBehavior.setBackgroundColor(this.view, Color.TRANSPARENT, 0f)
                    val ivContent = view.findViewById<ImageView>(R.id.ivContent)
                    val ivClose = view.findViewById<ImageView>(R.id.ivClose)
                    ivContent.clickN {
                        if (adModel.link != null && adModel.link.isNotEmpty()) {
                            context.startKtxActivity<WebActivity>(
                                values = listOf(
                                    Pair("url", adModel.link),
                                    Pair("type", "ad")
                                )
                            )
                        }
                    }
                    ivClose.clickN {
                        this.dismiss()
                    }
                }
            }
    }
}