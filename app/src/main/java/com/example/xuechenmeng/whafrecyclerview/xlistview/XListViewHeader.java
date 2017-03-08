/**
 * @file XListViewHeader.java
 * @create Apr 18, 2012 5:22:27 PM
 * @author Maxwin
 * @description XListView's header
 */
package com.example.xuechenmeng.whafrecyclerview.xlistview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.xuechenmeng.whafrecyclerview.R;


public class XListViewHeader extends LinearLayout {
    private LinearLayout mContainer;
    private int mState = STATE_NORMAL;

    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;

    private TextView mTextShow;
    private ImageView mImageShow;
    private ProgressBar progressBar;
    private RotateAnimation animationDrawable;
    private static final int DURATION_TRANSITION = 150;

    public XListViewHeader(Context context) {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public XListViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        // 初始情况，设置下拉刷新view高度为0
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.xlistview_header, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);


        mTextShow = (TextView) findViewById(R.id.pull_to_refresh_text);
        mImageShow = (ImageView) findViewById(R.id.pull_to_refresh_image_arrow);
        progressBar = (ProgressBar) findViewById(R.id.pull_to_refresh_image_bar);

        animationDrawable = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animationDrawable.setInterpolator(new LinearInterpolator());
        animationDrawable.setDuration(DURATION_TRANSITION);
        animationDrawable.setFillAfter(true);

    }

    public void setState(int state) {
        if (state == mState) return;
        switch (state) {
            case STATE_NORMAL:
                progressBar.setVisibility(View.INVISIBLE);
                mImageShow.setVisibility(View.VISIBLE);
                if (null != animationDrawable) {
                    animationDrawable.cancel();
                    mImageShow.clearAnimation();
                }
                mTextShow.setText(R.string.xlistview_header_hint_normal);
                break;
            case STATE_READY:
                mImageShow.setVisibility(View.VISIBLE);
                if (null != animationDrawable) {
//                    mImageShow.setImageDrawable(animationDrawable);
                    mImageShow.startAnimation(animationDrawable);
                }
                mTextShow.setText(R.string.xlistview_header_hint_ready);
                break;
            case STATE_REFRESHING:
                mImageShow.setVisibility(View.GONE);
                mTextShow.setText(R.string.xlistview_header_hint_loading);
                progressBar.setVisibility(View.VISIBLE);
                mImageShow.clearAnimation();
                break;
            default:
                break;
        }

        mState = state;
    }

    public void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisiableHeight() {
        return mContainer.getHeight();
    }

}
