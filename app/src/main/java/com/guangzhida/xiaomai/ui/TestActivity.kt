package com.guangzhida.xiaomai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.guangzhida.xiaomai.R
import com.guangzhida.xiaomai.utils.Base64Utils
import com.guangzhida.xiaomai.utils.RSAUtil
import com.guangzhida.xiaomai.view.SwipeItemLayout
import kotlinx.android.synthetic.main.activity_test.*


/**
 * 测试左右滑动
 */
class TestActivity : AppCompatActivity() {
    private val list = mutableListOf("", "", "")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addOnItemTouchListener(SwipeItemLayout.OnSwipeItemTouchListener(this))
        recyclerView.addItemDecoration(getRecyclerViewDivider(R.drawable.inset_recyclerview_divider));//设置分割线
        recyclerView.adapter = MyAdapter2(list)

        btn.setOnClickListener {
            setRSA()
        }
    }

    /**
     * 获取分割线
     *
     * @param drawableId 分割线id
     * @return
     */
    private fun getRecyclerViewDivider(@DrawableRes drawableId: Int): ItemDecoration {
        val itemDecoration =
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecoration.setDrawable(resources.getDrawable(drawableId))
        return itemDecoration
    }

    inner class MyAdapter2(list: MutableList<String>) : BaseQuickAdapter<String, BaseViewHolder>(
        R.layout.adapter_conversation_layout,
        list
    ) {
        override fun convert(helper: BaseViewHolder, item: String) {
        }

    }

    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val root = LayoutInflater.from(this@TestActivity)
                .inflate(R.layout.adapter_conversation_layout, parent, false);
            return MyViewHolder(root)
        }

        override fun getItemCount(): Int = 3

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        }

    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    private fun setRSA() { // 获取到密钥对
        val keyPair = RSAUtil.generateRSAKeyPair(1024)
        // 获取公钥和私钥
        val aPublic = keyPair.public
        val aPrivate = keyPair.private
        val aPublicEncoded = aPublic.encoded
        val aPrivateEncoded = aPrivate.encoded
        try { // 公钥加密
            val bytes =
                RSAUtil.encryptByPublicKey("java", "123".toByteArray(), aPublicEncoded)
            val encode = Base64Utils.encode(bytes)
            println("公钥加密文件:$encode")
            //            Log.d(TAG, "公钥加密文件: " + encode);
            val encodeBytes = Base64Utils.decode(encode)
            // 私钥解密
            val bytes1 =
                RSAUtil.decryptByPrivateKey("java", encodeBytes, aPrivateEncoded)
            val s = String(bytes1)
            println("公钥解密文件:$s")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //        System.out.println("");
//
//        try {
//            // 私钥加密
//            byte[] bytes = RSAUtil.encryptByPrivateKey(type, "456".getBytes(), aPrivateEncoded);
//            String encode = Base64Util.encode(bytes);
//            Log.d(TAG, "私钥加密文件: " + encode);
//
//            // 公钥解密
//            byte[] bytes1 = RSAUtil.decryptByPublicKey(type, bytes, aPublicEncoded);
//            String s = new String(bytes1);
//            Log.d(TAG, "公钥解密文件: " + s);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}