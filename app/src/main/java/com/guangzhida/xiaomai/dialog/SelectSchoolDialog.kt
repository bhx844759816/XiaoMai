package com.guangzhida.xiaomai.dialog

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.ItemListener
import com.afollestad.materialdialogs.list.checkItems
import com.afollestad.materialdialogs.list.listItems

/**
 * 选择学校的Dialog
 */
object SelectSchoolDialog {


    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        array: List<String>,
        callback: ((Int) -> Unit)?
    ) {
        MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT))
            .listItems(items = array, waitForPositiveButton = false,selection = object : ItemListener {
                override fun invoke(dialog: MaterialDialog, index: Int, text: CharSequence) {
                    callback?.invoke(index)
                }
            })
            .lifecycleOwner(owner)
            .show()
    }
}