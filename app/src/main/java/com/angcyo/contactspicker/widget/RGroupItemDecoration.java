package com.angcyo.contactspicker.widget;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

/**
 * 支持分组的ItemDecoration, 暂且只支持LinearLayoutManager
 * Created by angcyo on 2017-01-15.
 */

public class RGroupItemDecoration extends RecyclerView.ItemDecoration {

    private GroupCallBack mGroupCallBack;

    public RGroupItemDecoration(GroupCallBack groupCallBack) {
        mGroupCallBack = groupCallBack;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mGroupCallBack == null) {
            return;
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            final View view = parent.getChildAt(i);
            final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
            final int adapterPosition = layoutParams.getViewAdapterPosition();
            if (adapterPosition == 0) {
                //第一个位置, 肯定是有分组信息的
                mGroupCallBack.onGroupDraw(c, view, adapterPosition);
            } else {
                //上一个分组信息
                String preGroupText = mGroupCallBack.getGroupText(adapterPosition - 1);
                //当前的分组信息
                String groupText = mGroupCallBack.getGroupText(adapterPosition);
                if (!TextUtils.equals(preGroupText, groupText)) {
                    //如果和上一个分组信息不相等
                    mGroupCallBack.onGroupDraw(c, view, adapterPosition);
                }
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mGroupCallBack == null || parent.getChildCount() <= 0) {
            return;
        }

        boolean isHorizontal = ((LinearLayoutManager) parent.getLayoutManager()).getOrientation() == LinearLayoutManager.HORIZONTAL;

        final View view = parent.getChildAt(0);
        final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        final int adapterPosition = layoutParams.getViewAdapterPosition();
        if (adapterPosition == 0) {
            //第一个位置, 肯定是有分组信息的
            if ((isHorizontal ? view.getLeft() : view.getTop()) <= 0) {
                mGroupCallBack.onGroupOverDraw(c, view, adapterPosition, 0);
            } else {
                mGroupCallBack.onGroupOverDraw(c, view, adapterPosition, mGroupCallBack.getGroupHeight() - (isHorizontal ? view.getLeft() : view.getTop()));
            }
        } else {
            if (parent.getLayoutManager().getItemCount() > adapterPosition + 1) {
                //下一个分组信息
                String nextGroupText = mGroupCallBack.getGroupText(adapterPosition + 1);
                //当前的分组信息
                String groupText = mGroupCallBack.getGroupText(adapterPosition);

                final View nextView = parent.getChildAt(1);
                if (!TextUtils.equals(nextGroupText, groupText)) {
                    if ((isHorizontal ? nextView.getLeft() : nextView.getTop()) <= 0) {
                        mGroupCallBack.onGroupOverDraw(c, view, adapterPosition, 0);
                    } else {
                        mGroupCallBack.onGroupOverDraw(c, view, adapterPosition,
                                Math.max(0, 2 * mGroupCallBack.getGroupHeight() - (isHorizontal ? nextView.getLeft() : nextView.getTop())));
                    }
                } else {
                    mGroupCallBack.onGroupOverDraw(c, view, adapterPosition, 0);
                }
            } else {
                mGroupCallBack.onGroupOverDraw(c, view, adapterPosition, 0);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();//布局管理器
        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new IllegalArgumentException("暂不支持 " + layoutManager.getClass().getSimpleName());
        }

        if (mGroupCallBack == null) {
            return;
        }

        final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        final int adapterPosition = layoutParams.getViewAdapterPosition();
        if (adapterPosition == 0) {
            //第一个位置, 肯定是有分组信息的
            if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                outRect.set(mGroupCallBack.getGroupHeight(), 0, 0, 0);
            } else {
                outRect.set(0, mGroupCallBack.getGroupHeight(), 0, 0);
            }
        } else {
            //上一个分组信息
            String preGroupText = mGroupCallBack.getGroupText(adapterPosition - 1);
            //当前的分组信息
            String groupText = mGroupCallBack.getGroupText(adapterPosition);
            if (!TextUtils.equals(preGroupText, groupText)) {
                //如果和上一个分组信息不相等
                if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    outRect.set(mGroupCallBack.getGroupHeight(), 0, 0, 0);
                } else {
                    outRect.set(0, mGroupCallBack.getGroupHeight(), 0, 0);
                }
            }
        }
    }

    public interface GroupCallBack {
        /**
         * 返回分组的高度
         */
        int getGroupHeight();

        /**
         * 返回分组的文本
         */
        String getGroupText(int position);

        /**
         * 绘制分组信息
         */
        void onGroupDraw(Canvas canvas, View view, int position);

        /**
         * 绘制悬浮信息
         *
         * @param offset 需要偏移的距离
         */
        void onGroupOverDraw(Canvas canvas, View view, int position, int offset);
    }
}
