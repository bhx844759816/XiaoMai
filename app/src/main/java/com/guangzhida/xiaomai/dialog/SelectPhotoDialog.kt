package com.guangzhida.xiaomai.dialog

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.ItemListener
import com.afollestad.materialdialogs.list.listItems

/**
 * 选择图片和拍照的Dialog
 */
object SelectPhotoDialog {
    private val items = arrayListOf("拍照", "从相册选择")
    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        callback: ((Int) -> Unit)?
    ) {
        MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT))
            .listItems(
                items = items,
                waitForPositiveButton = false,
                selection = object : ItemListener {
                    override fun invoke(dialog: MaterialDialog, index: Int, text: CharSequence) {
                        callback?.invoke(index)
                    }
                })
            .lifecycleOwner(owner)
            .show()
    }
}