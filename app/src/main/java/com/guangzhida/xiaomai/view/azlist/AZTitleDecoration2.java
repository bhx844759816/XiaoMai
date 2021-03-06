package com.guangzhida.xiaomai.view.azlist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.guangzhida.xiaomai.ui.chat.adapter.ContactListAdapter2;
import com.guangzhida.xiaomai.utils.LogUtils;
import com.guangzhida.xiaomai.utils.TextDrawUtils;


public class AZTitleDecoration2 extends RecyclerView.ItemDecoration {

    private TextPaint mTitleTextPaint;
    private Paint mBackgroundPaint;
    private TitleAttributes mTitleAttributes;
    private int mHeaderCount;

    public AZTitleDecoration2(TitleAttributes attributes, int headerCount) {
        mTitleAttributes = attributes;
        mHeaderCount = headerCount;
        mTitleTextPaint = new TextPaint();
        mTitleTextPaint.setAntiAlias(true);
        mTitleTextPaint.setTextSize(mTitleAttributes.mTextSize);
        mTitleTextPaint.setColor(mTitleAttributes.mTextColor);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(mTitleAttributes.mBackgroundColor);
    }

    /**
     * 绘制标题
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (parent.getAdapter() == null) {
            return;
        }
        ContactListAdapter2 adapter = (ContactListAdapter2) parent.getAdapter();
        if (adapter.getData().isEmpty()) {
            return;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            final View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position >= mHeaderCount) {
                if (titleAttachView(child, parent)) {
                    drawTitleItem(c, parent, child, adapter.getSortLetters(position));
                }
            }

        }
    }

    /**
     * 绘制悬浮标题
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
//        if (parent.getAdapter() == null) {
//            return;
//        }
//        ContactListAdapter2 adapter = (ContactListAdapter2) parent.getAdapter();
//        if (adapter.getData().isEmpty()) {
//            return;
//        }
//        View firstView = parent.getChildAt(0);
//        int firstAdapterPosition = parent.getChildAdapterPosition(firstView);
//        c.save();
//        //找到下一个标题对应的adapter position
//        int nextLetterAdapterPosition = adapter.getNextSortLetterPosition(firstAdapterPosition);
//        if (nextLetterAdapterPosition != -1) {
//            //下一个标题view index
//            int nextLettersViewIndex = nextLetterAdapterPosition - firstAdapterPosition;
//            if (nextLettersViewIndex < parent.getChildCount()) {
//                View nextLettersView = parent.getChildAt(nextLettersViewIndex);
//                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) nextLettersView.getLayoutParams();
//                int nextToTop = nextLettersView.getTop() - params.bottomMargin - parent.getPaddingTop();
//                if (nextToTop < mTitleAttributes.mItemHeight * 2) {
//                    //有重叠
//                    c.translate(0, nextToTop - mTitleAttributes.mItemHeight * 2);
//                }
//            }
//        }
//        mBackgroundPaint.setColor(mTitleAttributes.mBackgroundColor);
//        c.drawRect(parent.getPaddingLeft(), parent.getPaddingTop(), parent.getRight() - parent.getPaddingRight(),
//                parent.getPaddingTop() + mTitleAttributes.mItemHeight, mBackgroundPaint);
//        mTitleTextPaint.setTextSize(mTitleAttributes.mTextSize);
//        mTitleTextPaint.setColor(mTitleAttributes.mTextColor);
//        c.drawText(adapter.getSortLetters(firstAdapterPosition),
//                parent.getPaddingLeft() + firstView.getPaddingLeft() + mTitleAttributes.mTextPadding,
//                TextDrawUtils.getTextBaseLineByCenter(parent.getPaddingTop() + mTitleAttributes.mItemHeight / 2, mTitleTextPaint),
//                mTitleTextPaint);
//        c.restore();
    }

    /**
     * 设置空出绘制标题的区域
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (titleAttachView(view, parent)) {
            outRect.set(0, mTitleAttributes.mItemHeight, 0, 0);
        } else {
            super.getItemOffsets(outRect, view, parent, state);
        }
    }

    /**
     * 绘制标题信息
     */
    private void drawTitleItem(Canvas c, RecyclerView parent, View child, String letters) {
        final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        //绘制背景
        c.drawRect(parent.getPaddingLeft(), child.getTop() - params.bottomMargin - mTitleAttributes.mItemHeight,
                parent.getWidth() - parent.getPaddingRight(), child.getTop() - params.bottomMargin, mBackgroundPaint);

        float textCenterY = child.getTop() - params.bottomMargin - mTitleAttributes.mItemHeight / 2;
        //绘制标题文字
        c.drawText(letters, parent.getPaddingLeft() + child.getPaddingLeft() + mTitleAttributes.mTextPadding,
                TextDrawUtils.getTextBaseLineByCenter(textCenterY, mTitleTextPaint), mTitleTextPaint);
    }

    /**
     * 判断指定view的上方是否要空出绘制标题的位置
     *
     * @param view   　指定的view
     * @param parent 父view
     */
    private boolean titleAttachView(View view, RecyclerView parent) {
        if (parent.getAdapter() == null || !(parent.getAdapter() instanceof ContactListAdapter2)) {
            return false;
        }
        ContactListAdapter2 adapter = (ContactListAdapter2) parent.getAdapter();
        if (adapter.getData().isEmpty()) {
            return false;
        }
        int position = parent.getChildAdapterPosition(view);
        if (position < mHeaderCount) {
            return false;
        } else {
            return position == mHeaderCount ||(null != adapter.getData().get(position-mHeaderCount) && !adapter.getSortLetters(position ).equals(adapter.getSortLetters(position - 1)));
        }
    }

    public static class TitleAttributes {

        Context mContext;
        /**
         * 标题高度
         */
        int mItemHeight;
        /**
         * 文字的padding
         */
        int mTextPadding;
        /**
         * 标题文字大小
         */
        int mTextSize;
        /**
         * 标题文字颜色
         */
        int mTextColor;
        /**
         * 标题背景
         */
        int mBackgroundColor;

        public TitleAttributes(Context context) {
            mContext = context;
            mItemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
            mTextPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, context.getResources().getDisplayMetrics());
            mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, context.getResources().getDisplayMetrics());
            mTextColor = Color.parseColor("#FF000000");
            mBackgroundColor = Color.parseColor("#FFDFDFDF");
        }

        public TitleAttributes setItemHeight(int heightDp) {
            mItemHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp,
                    mContext.getResources().getDisplayMetrics());
            return this;
        }

        public TitleAttributes setTextPadding(int paddingDp) {
            mTextPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingDp,
                    mContext.getResources().getDisplayMetrics());
            return this;
        }

        public TitleAttributes setTextSize(int sizeSp) {
            mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sizeSp, mContext.getResources().getDisplayMetrics());
            return this;
        }

        public TitleAttributes setTextColor(int color) {
            mTextColor = color;
            return this;
        }

        public TitleAttributes setBackgroundColor(int color) {
            mBackgroundColor = color;
            return this;
        }


    }
}
