package com.guangzhida.xiaomai.view.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.guangzhida.xiaomai.R;

import java.util.ArrayList;


public class SimpleAppsGridView extends RelativeLayout {

    protected View view;

    public SimpleAppsGridView(Context context) {
        this(context, null);
    }

    public SimpleAppsGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.layout_chat_simple_app_view_apps, this);
        init();
    }

    protected void init(){
        GridView gv_apps = (GridView) view.findViewById(R.id.gv_apps);
        gv_apps.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gv_apps.setNumColumns(2);
        ArrayList<AppBean> mAppBeanList = new ArrayList<>();
        mAppBeanList.add(new AppBean(R.mipmap.chatting_photo, "图片"));
        mAppBeanList.add(new AppBean(R.mipmap.chatting_camera, "拍照"));
        ChattingAppsAdapter adapter = new ChattingAppsAdapter(getContext(), mAppBeanList);
        gv_apps.setAdapter(adapter);
    }
}
