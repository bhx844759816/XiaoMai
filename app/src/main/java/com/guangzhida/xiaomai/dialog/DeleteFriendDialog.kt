package com.guangzhida.xiaomai.dialog

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.ItemListener
import com.afollestad.materialdialogs.list.listItems
import com.guangzhida.xiaomai.R

/**
 * 删除好友
 */
object DeleteFriendDialog {
    fun showDialog(
        context: Context,
        owner: LifecycleOwner,
        item:String,
        callback: (() -> Unit)?
    ) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.dialog_chat_delete_friend, null)
        val tvDeleteFriend = view.findViewById<TextView>(R.id.tvDeleteFriend)
        val tvCancel = view.findViewById<TextView>(R.id.tvCancel)
        val dialog = MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT))
            .customView(view = view, noVerticalPadding = true)
            .lifecycleOwner(owner)
        tvDeleteFriend.text = item
        tvDeleteFriend.setOnClickListener {
            dialog.dismiss()
            callback?.invoke()
        }
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()


    }
}