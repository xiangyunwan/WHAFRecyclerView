package com.example.xuechenmeng.whafrecyclerview;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;  
  
/** 
 *  
 */  
public class XRecyclerView extends RecyclerView {
    private int footerHeight = -1;  
    LinearLayoutManager layoutManager;  
    // -- footer view  
    private CustomDragFooterView mFooterView;
    private boolean mEnablePullLoad;  
    private boolean mPullLoading;  
    private boolean isBottom;  
    private boolean mIsFooterReady = false;  
    private LoadMoreListener loadMoreListener;  
  
    // -- header view  
    private CustomDragHeaderView mHeaderView;  
    private boolean mEnablePullRefresh = true;  
    private boolean mIsRefreshing;  
    private boolean isHeader;  
    private boolean mIsHeaderReady = false;  
    private Timer timer;  
    private float oldY;  
    Handler handler = new Handler();  
    private OnRefreshListener refreshListener;  
    private XRecyclerViewAdapter adapter;
    private int maxPullHeight = 50;//最多下拉高度的px值  
  
    private static final int HEADER_HEIGHT = 68;//头部高度68dp  
    private static final int MAX_PULL_LENGTH = 150;//最多下拉150dp  
    private OnClickListener footerClickListener;  
  
  
    public XRecyclerView(Context context) {
        super(context);  
        initView(context);  
    }  
  
    public XRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);  
        initView(context);  
    }  
  
    public XRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);  
        initView(context);  
    }  
  
    public void setAdapter(XRecyclerViewAdapter adapter){
        super.setAdapter(adapter);  
        this.adapter = adapter;  
    }  
  
    public boolean ismPullLoading() {  
        return mPullLoading;  
    }  
  
    public boolean ismIsRefreshing() {  
        return mIsRefreshing;  
    }  
  
    private void updateFooterHeight(float delta) {
        if(mFooterView==null)return;  
        int bottomMargin = mFooterView.getBottomMargin();  
//        Log.i("Alex3","初始delta是"+delta);  
        if(delta>50)delta = delta/6;  
        if(delta>0) {//越往下滑越难滑  
            if(bottomMargin>maxPullHeight)delta = delta*0.65f;  
            else if(bottomMargin>maxPullHeight * 0.83333f)delta = delta*0.7f;  
            else if(bottomMargin>maxPullHeight * 0.66667f)delta = delta*0.75f;  
            else if(bottomMargin>maxPullHeight >> 1)delta = delta*0.8f;  
            else if(bottomMargin>maxPullHeight * 0.33333f)delta = delta*0.85f;  
            else if(bottomMargin>maxPullHeight * 0.16667F && delta > 20)delta = delta*0.2f;//如果是因为惯性向下迅速的俯冲  
            else if(bottomMargin>maxPullHeight * 0.16667F)delta = delta*0.9f;  
//            Log.i("Alex3","bottomMargin是"+mFooterView.getBottomMargin()+" delta是"+delta);  
        }  
  
        int height = mFooterView.getBottomMargin() + (int) (delta+0.5);  
  
        if (mEnablePullLoad && !mPullLoading) {  
            if (height > 150){//必须拉超过一定距离才加载更多  
//            if (height > 1){//立即刷新  
                mFooterView.setState(CustomDragFooterView.STATE_READY);
                mIsFooterReady = true;  
//                Log.i("Alex2", "ready");  
            } else {  
                mFooterView.setState(CustomDragFooterView.STATE_NORMAL);
                mIsFooterReady = false;  
//                Log.i("Alex2", "nomal");  
            }  
        }  
        mFooterView.setBottomMargin(height);  
  
  
    }  
  
    private void resetFooterHeight() {
        int bottomMargin = mFooterView.getBottomMargin();
        if (bottomMargin > 20) {  
            Log.i("Alex2", "准备重置高度,margin是" + bottomMargin + "自高是" + footerHeight);  
            this.smoothScrollBy(0,-bottomMargin);  
            //一松手就立即开始加载  
            if(mIsFooterReady){  
                startLoadMore();  
            }  
        }  
    }  
  
  
    public void setLoadMoreListener(LoadMoreListener listener){  
        this.loadMoreListener = listener;  
    }  
  
    public void initView(Context context){  
        layoutManager = new LinearLayoutManager(context);//自带layoutManager，请勿设置  
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);  
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);  
        int height = wm.getDefaultDisplay().getHeight();  
        layoutManager.offsetChildrenVertical(height*2);//预加载2/3的卡片  
        this.setLayoutManager(layoutManager);  
//        Log.i("Alex", "屏幕密度为" + getContext().getResources().getDisplayMetrics().density);  
        maxPullHeight = dp2px(getContext().getResources().getDisplayMetrics().density,MAX_PULL_LENGTH);//最多下拉150dp  
        this.footerClickListener = new footerViewClickListener();  
        this.addOnScrollListener(new OnScrollListener() {
  
            @Override  
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {  
                super.onScrollStateChanged(recyclerView, newState);  
                switch (newState){  
                    case RecyclerView.SCROLL_STATE_IDLE:  
//                        Log.i("Alex2", "停下了||放手了");  
                        if(isBottom)resetFooterHeight();  
                        break;  
                    case RecyclerView.SCROLL_STATE_DRAGGING:  
//                        Log.i("Alex2", "开始拖了,现在margin是" + (mFooterView == null ? "" : mFooterView.getBottomMargin()));  
                        break;  
                    case RecyclerView.SCROLL_STATE_SETTLING:  
//                        Log.i("Alex2", "开始惯性移动");  
                        break;  
                }  
  
            }  
  
            @Override  
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {  
                super.onScrolled(recyclerView, dx, dy);  
                int lastItemPosition = layoutManager.findLastVisibleItemPosition();  
//                Log.i("Alex2","mEnable是"+mEnablePullLoad+"lastitemPosition是"+lastItemPosition+" itemcount是"+layoutManager.getItemCount());  
                if(lastItemPosition == layoutManager.getItemCount()-1 && mEnablePullLoad) {//如果到了最后一个  
                    isBottom = true;  
                    mFooterView = (CustomDragFooterView)layoutManager.findViewByPosition(layoutManager.findLastVisibleItemPosition());//一开始还不能hide，因为hide得到最后一个可见的就不是footerview了
                    Log.i("xue","到底啦！！"+"mfooterView是"+mFooterView);
                    if(mFooterView!=null) mFooterView.setOnClickListener(footerClickListener);  
                    if(footerHeight==-1 && mFooterView!=null){  
                        mFooterView.show();  
                        mFooterView.setState(CustomDragFooterView.STATE_NORMAL);
                        footerHeight = mFooterView.getMeasuredHeight();//这里的测量一般不会出问题  
//                        Log.i("Alex2", "底部高度为" + footerHeight);  
                    }  
                    updateFooterHeight(dy);  
                }else if(lastItemPosition == layoutManager.getItemCount()-1 && mEnablePullLoad){//如果到了倒数第二个  
                    startLoadMore();//开始加载更多  
                }  
                else {  
                    isBottom = false;  
                }  
            }  
        });  
    }  
  
    /** 
     * 设置是否开启上拉加载更多的功能 
     * 
     * @param enable 
     */  
    public void setPullLoadEnable(boolean enable) {  
        mPullLoading = false;  
        mEnablePullLoad = enable;  
        if(adapter!=null)adapter.setPullLoadMoreEnable(enable);//adapter和recyclerView要同时设置  
        if(mFooterView==null)return;  
        if (!mEnablePullLoad) {  
//            this.smoothScrollBy(0,-footerHeight);  
            mFooterView.setFooterText();
            mFooterView.setOnClickListener(null);  
            mFooterView.setBottomMargin(0);  
            //make sure "pull up" don't show a line in bottom when listview with one page  
        } else {  
            mFooterView.show();  
            mFooterView.setState(CustomDragFooterView.STATE_NORMAL);
            mFooterView.setVisibility(VISIBLE);  
            //make sure "pull up" don't show a line in bottom when listview with one page  
            // both "pull up" and "click" will invoke load more.  
            mFooterView.setOnClickListener(new OnClickListener() {  
                @Override  
                public void onClick(View v) {  
                    startLoadMore();  
                }  
            });  
        }  
    }  
  
    /** 
     * 停止loadmore 
     */  
    public void stopLoadMore() {  
        if (mPullLoading == true) {  
            mPullLoading = false;  
            if(mFooterView==null)return;  
            mFooterView.show();  
            mFooterView.setState(CustomDragFooterView.STATE_NO_MORE);
        }  
    }  
  
    private void startLoadMore() {  
        if(mPullLoading)return;  
        mPullLoading = true;  
        if(mFooterView!=null)mFooterView.setState(CustomDragFooterView.STATE_LOADING);
        Log.i("Alex2", "现在开始加载");  
        mIsFooterReady = false;  
        if (loadMoreListener != null) {  
            loadMoreListener.onLoadMore();  
        }  
    }  
  
    /** 
     * 在刷新时要执行的方法 
     */  
    public interface LoadMoreListener{  
        public void onLoadMore();  
    }  
  
    /** 
     * 点击loadMore后要执行的事件 
     */  
    class footerViewClickListener implements OnClickListener {  
  
        @Override  
        public void onClick(View v) {  
            startLoadMore();  
        }  
    }  
  
  
    private void updateHeaderHeight(float delta) {  
        mHeaderView = (CustomDragHeaderView) layoutManager.findViewByPosition(0);  
//        Log.i("Alex2", "现在在头部！！！！ header自高是" + mHeaderView.getHeight() + "   margin 是" + mHeaderView.getTopMargin());//自高一般不会算错  
//        Log.i("Alex2", "正在设置margin" + mHeaderView.getTopMargin() +"delta是"+delta);  
        if(delta>0){//如果是往下拉  
            int topMargin = mHeaderView.getTopMargin();  
            if(topMargin>maxPullHeight * 0.33333f)delta = delta*0.5f;  
            else if(topMargin>maxPullHeight * 0.16667F)delta = delta*0.55f;  
            else if(topMargin>0)delta = delta*0.6f;  
            else if(topMargin<0)delta = delta*0.6f;//如果没有被完全拖出来  
            mHeaderView.setTopMargin(mHeaderView.getTopMargin() + (int)delta);  
        } else{//如果是推回去  
            if(!mIsRefreshing || mHeaderView.getTopMargin()>0) {//在刷新的时候不把margin设为负值以在惯性滑动的时候能滑回去  
                this.scrollBy(0, (int) delta);//禁止既滚动，又同时减少触摸  
//                Log.i("Alex2", "正在往回推" + delta);  
                mHeaderView.setTopMargin(mHeaderView.getTopMargin() + (int) delta);  
            }  
        }  
        if(mHeaderView.getTopMargin()>0 && !mIsRefreshing){  
            mIsHeaderReady = true;  
            mHeaderView.setState(CustomDragHeaderView.STATE_READY);  
        }//设置为ready状态  
        else if(!mIsRefreshing){  
            mIsHeaderReady = false;  
            mHeaderView.setState(CustomDragHeaderView.STATE_NORMAL);  
        }//设置为普通状态并且缩回去  
    }  
  
    @Override  
    public void smoothScrollToPosition(final int position) {  
        super.smoothScrollToPosition(position);  
        final Timer scrollTimer = new Timer();  
        TimerTask timerTask = new TimerTask() {  
            @Override  
            public void run() {  
                int bottomCardPosition = layoutManager.findLastVisibleItemPosition();  
                if(bottomCardPosition<position+1){//如果要向下滚  
                    handler.post(new Runnable() {  
                        @Override  
                        public void run() {  
                            smoothScrollBy(0,50);  
                        }  
                    });  
                }else if(bottomCardPosition>position){//如果要向上滚  
                    handler.post(new Runnable() {  
                        @Override  
                        public void run() {  
                            smoothScrollBy(0,-50);  
                        }  
                    });  
                }else {  
                    if(scrollTimer!=null)scrollTimer.cancel();  
                }  
            }  
        };  
        scrollTimer.schedule(timerTask,0,20);  
  
    }  
  
    /** 
     * 在用户非手动强制刷新的时候，通过一个动画把头部一点点冒出来 
     */  
    private void smoothShowHeader(){  
        if(mHeaderView==null)return;  
//        if(layoutManager.findFirstVisibleItemPosition()!=0){//如果刷新完毕的时候用户没有注视header  
//            mHeaderView.setTopMargin(0);  
//            return;  
//        }  
        if(timer!=null)timer.cancel();  
        final TimerTask timerTask = new TimerTask() {  
            @Override  
            public void run() {  
                if(mHeaderView==null){  
                    if(timer!=null)timer.cancel();  
                    return;  
                }  
//                Log.i("Alex2","topMargin是"+mHeaderView.getTopMargin()+" height是"+mHeaderView.getHeight());  
                if(mHeaderView.getTopMargin()<0){  
                    handler.post(new Runnable() {  
                        @Override  
                        public void run() {  
                            if (mIsRefreshing) {//如果目前是ready状态或者正在刷新状态  
                                mHeaderView.setTopMargin(mHeaderView.getTopMargin() +2);  
                            }  
                        }  
                    });  
                } else if(timer!=null){//如果已经完全缩回去了，但是动画还没有结束，就结束掉动画  
                    timer.cancel();  
                }  
            }  
        };  
        timer = new Timer();  
        timer.scheduleAtFixedRate(timerTask,0,16);  
    }  
  
    /** 
     * 在用户松手的时候让头部自动收缩回去 
     */  
    private void resetHeaderHeight() {  
        if(mHeaderView==null)mHeaderView = (CustomDragHeaderView) layoutManager.findViewByPosition(0);  
        if(layoutManager.findFirstVisibleItemPosition()!=0){//如果刷新完毕的时候用户没有注视header  
            mHeaderView.setTopMargin(-mHeaderView.getRealHeight());  
            return;  
        }  
        if(timer!=null)timer.cancel();  
        final TimerTask timerTask = new TimerTask() {  
            @Override  
            public void run() {  
                if(mHeaderView==null)return;  
//                Log.i("Alex2","topMargin是"+mHeaderView.getTopMargin()+" height是"+mHeaderView.getHeight());  
                if(mHeaderView.getTopMargin()>-mHeaderView.getRealHeight()){//如果header没有完全缩回去  
                    handler.post(new Runnable() {  
                        @Override  
                        public void run() {  
                            if (mIsHeaderReady || mIsRefreshing) {//如果目前是ready状态或者正在刷新状态  
//                                Log.i("Alex2", "现在是ready状态");  
                                int delta = mHeaderView.getTopMargin() / 9;  
                                if (delta < 5) delta = 5;  
                                if (mHeaderView.getTopMargin() > 0)  
                                    mHeaderView.setTopMargin(mHeaderView.getTopMargin() - delta);  
                            } else {//如果是普通状态  
//                                Log.i("Alex2", "现在是普通状态");  
                                mHeaderView.setTopMargin(mHeaderView.getTopMargin() - 5);  
                            }  
                        }  
                    });  
                } else if(timer!=null){//如果已经完全缩回去了，但是动画还没有结束，就结束掉动画  
                    timer.cancel();  
                    handler.post(new Runnable() {  
                        @Override  
                        public void run() {  
                            mHeaderView.setState(mHeaderView.STATE_FINISH);  
                        }  
                    });  
                }  
            }  
        };  
        timer = new Timer();  
        timer.scheduleAtFixedRate(timerTask,0,10);  
    }  
  
  
    /** 
     * 头部是通过onTouchEvent控制的 
     * @param event 
     * @return 
     */  
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
  
        switch (event.getAction()) {  
            case MotionEvent.ACTION_MOVE:  
                int delta = (int)(event.getY()-oldY);  
                oldY = event.getY();  
                if (layoutManager.findViewByPosition(0) instanceof CustomDragHeaderView) {  
                    isHeader = true;  
                    updateHeaderHeight(delta);//更新margin高度  
                }else{  
                    isHeader = false;  
                    if(mHeaderView!=null && !mIsRefreshing)mHeaderView.setTopMargin(-mHeaderView.getRealHeight());  
                }  
                break;  
//            case MotionEvent.ACTION_DOWN:  
//                Log.i("Alex", "touch down");  
//                oldY = event.getY();  
//                if(timer!=null)timer.cancel();  
//                break;  
            case MotionEvent.ACTION_UP:  
//                Log.i("Alex", "抬手啦！！！！ touch up ");  
                if(mIsHeaderReady && !mIsRefreshing)startRefresh();  
                if(isHeader)resetHeaderHeight();//抬手之后恢复高度  
                break;  
            case MotionEvent.ACTION_CANCEL:  
//                Log.i("Alex", "touch cancel");  
                break;  
  
        }  
        return super.onTouchEvent(event);  
    }  
  
    /** 
     * 因为设置了子元素的onclickListener之后，ontouch方法的down失效，所以要在分发前获取手指的位置 
     * @param ev 
     * @return 
     */  
    @Override  
    public boolean dispatchTouchEvent(MotionEvent ev) {  
        // TODO Auto-generated method stub  
        switch (ev.getAction()) {  
            case MotionEvent.ACTION_DOWN:  
//                Log.i("Alex", "touch down分发前");  
                oldY = ev.getY();  
                if (timer != null) timer.cancel();  
                break;  
        }  
        return super.dispatchTouchEvent(ev);  
    }  
  
    public void setOnRefreshListener(OnRefreshListener listener){  
        this.refreshListener = listener;  
    }  
  
    /** 
     * 设置是否支持下啦刷新的功能 
     * 
     * @param enable 
     */  
    public void setPullRefreshEnable(boolean enable) {  
        mIsRefreshing = false;  
        mEnablePullRefresh = enable;  
        if(mHeaderView==null)return;  
        if (!mEnablePullRefresh) {  
            mHeaderView.setOnClickListener(null);
            mHeaderView.setVisibility(INVISIBLE);
        } else {  
            mHeaderView.setState(CustomDragFooterView.STATE_NORMAL);  
            mHeaderView.setVisibility(VISIBLE);  
        }  
    }  
  
    /** 
     * 停止下拉刷新，并且通过动画让头部自己缩回去 
     */  
    public void stopRefresh() {  
        if (mIsRefreshing == true) {  
            mIsRefreshing = false;  
            mIsHeaderReady = false;  
            if(mHeaderView==null)return;  
            mHeaderView.setState(CustomDragFooterView.STATE_NORMAL);  
            resetHeaderHeight();  
        }  
    }  
  
    /** 
     * 在用户没有用手控制的情况下，通过动画把头部露出来并且执行刷新 
     */  
    public void forceRefresh(){  
        if(mHeaderView==null)mHeaderView = (CustomDragHeaderView) layoutManager.findViewByPosition(0);  
        if(mHeaderView!=null)mHeaderView.setState(CustomDragHeaderView.STATE_REFRESHING);  
        mIsRefreshing = true;  
        Log.i("Alex2", "现在开始强制刷新");  
        mIsHeaderReady = false;  
        smoothShowHeader();  
        if (refreshListener != null)refreshListener.onRefresh();  
  
  
    }  
  
  
    private void startRefresh() {  
        mIsRefreshing = true;  
        mHeaderView.setState(CustomDragHeaderView.STATE_REFRESHING);  
        Log.i("Alex2", "现在开始加载");  
        mIsHeaderReady = false;  
        if (refreshListener != null) refreshListener.onRefresh();  
  
    }  
  
    public interface OnRefreshListener{  
        public void onRefresh();  
    }  
  
  
    /** 
     * 适用于本recycler的头部下拉刷新view 
     */  
    public static class CustomDragHeaderView extends LinearLayout {  
        public final static int STATE_NORMAL = 0;  
        public final static int STATE_READY = 1;  
        public final static int STATE_REFRESHING = 2;  
        public final static int STATE_FINISH = 3;
        private static final int DURATION_TRANSITION = 150;
        public float screenDensity;  
        private final int ROTATE_ANIM_DURATION = 180;  
        private Context mContext;  
  
        private View mContentView;
        private TextView mTextShow;
        private ImageView mImageShow;
        private ProgressBar progressBar;
        private RotateAnimation animationDrawable;
  
        public CustomDragHeaderView(Context context) {  
            super(context);  
            initView(context);  
        }  
  
        public CustomDragHeaderView(Context context, AttributeSet attrs) {  
            super(context, attrs);  
            initView(context);  
        }  
  
  
        private int mState = STATE_NORMAL;
        public void setState(int state) {  
            if (state == mState)  
                return;
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
  
        public void setTopMargin(int height) {  
            if (mContentView==null) return ;  
            LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();  
            lp.topMargin = height;  
            mContentView.setLayoutParams(lp);  
        }  
        //  
        public int getTopMargin() {  
            LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();  
            return lp.topMargin;  
        }  
  
        public void setHeight(int height){  
            if (mContentView==null) return ;  
            LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();  
            lp.height = height;  
            mContentView.setLayoutParams(lp);  
        }  
  
        private int realHeight;  
  
        /** 
         * 得到这个headerView真实的高度，而且这个高度是自己定的 
         * @return 
         */  
        public int getRealHeight(){  
            return realHeight;  
        }  
  
        private void initView(Context context) {  
            mContext = context;  
            this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));//recyclerView里不加这句话的话宽度就会比较窄  
            LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.xlistview_header, null);  
            addView(moreView);  
            moreView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  
            mContentView = moreView.findViewById(R.id.xlistview_header_content);  
            LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();  
            Log.i("Alex", "初始height是" + mContentView.getHeight());  
            lp.height = 150;//手动设置高度,如果要手动加载更多的时候才设置  
            screenDensity = getContext().getResources().getDisplayMetrics().density;//设置屏幕密度，用来px向dp转化  
            lp.height = dp2px(screenDensity,HEADER_HEIGHT);//头部高度75dp  
            realHeight = lp.height;  
            lp.topMargin = -lp.height;  
            mContentView.setLayoutParams(lp);
            mTextShow = (TextView) findViewById(R.id.pull_to_refresh_text);
            mImageShow = (ImageView) findViewById(R.id.pull_to_refresh_image_arrow);
            progressBar = (ProgressBar) findViewById(R.id.pull_to_refresh_image_bar);

            animationDrawable = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            animationDrawable.setInterpolator(new LinearInterpolator());
            animationDrawable.setDuration(DURATION_TRANSITION);
            animationDrawable.setFillAfter(true);
        }  
    }  
  
    public static int dp2px(float density, int dp) {  
        if (dp == 0) {  
            return 0;  
        }  
        return (int) (dp * density + 0.5f);  
    }  
  
    public static class CustomDragFooterView extends LinearLayout {
        public final static int STATE_NORMAL = 0;  
        public final static int STATE_READY = 1;  
        public final static int STATE_LOADING = 2;  
        public final static int STATE_NO_MORE = 3;
  
        private Context mContext;  
  
        private View mContentView;  
        private View mProgressBar;  
        private TextView mHintView;  
  
        public CustomDragFooterView(Context context) {
            super(context);  
            initView(context);  
        }  
  
        public CustomDragFooterView(Context context, AttributeSet attrs) {
            super(context, attrs);  
            initView(context);  
        }  
  
  
        public void setState(int state) {
            mHintView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.GONE);
            if (state == STATE_READY) {
                mHintView.setVisibility(View.VISIBLE);
                mHintView.setText(R.string.xlistview_footer_hint_ready);
                mProgressBar.setVisibility(View.GONE);
            } else if (state == STATE_LOADING) {
                mHintView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mHintView.setText(R.string.xlistview_header_hint_loading);
            } else if (state == STATE_NO_MORE) {
                mHintView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                return;
            }else {
                mHintView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mHintView.setText(R.string.xlistview_footer_hint_normal);
            }
        }  
  
        public void setBottomMargin(int height) {  
            if (height < 0) return ;  
            LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();  
            lp.bottomMargin = height;  
            mContentView.setLayoutParams(lp);  
        }  
  
        public int getBottomMargin() {  
            LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();  
            return lp.bottomMargin;  
        }

        public void setFooterText() {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText("没有更多了");
        }

        /** 
         * normal status 
         */  
        public void normal() {  
            mHintView.setVisibility(View.VISIBLE);  
            mProgressBar.setVisibility(View.GONE);  
        }  
  
  
        /** 
         * loading status 
         */  
        public void loading() {  
            mHintView.setVisibility(View.GONE);  
            mProgressBar.setVisibility(View.VISIBLE);  
        }  
  
        /** 
         * hide footer when disable pull load more 
         */  
        public void hide() {  
            LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();  
            lp.height = 1;//这里如果设为0那么layoutManger就会抓不到  
            mContentView.setLayoutParams(lp);  
            mContentView.setBackgroundColor(Color.BLACK);//这里的颜色要和自己的背景色一致  
        }  
  
        /** 
         * show footer 
         */  
        public void show() {  
            LayoutParams lp = (LayoutParams)mContentView.getLayoutParams();  
            lp.height = LayoutParams.WRAP_CONTENT;  
            lp.width =  LayoutParams.MATCH_PARENT;  
            mContentView.setLayoutParams(lp);  
//            mContentView.setBackgroundColor(Color.WHITE);//这里的颜色要和自己的背景色一致
        }  
  
        private void initView(Context context) {  
            mContext = context;  
            this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  
            LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.xlistview_footer, null);  
            addView(moreView);  
            moreView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            mContentView = moreView.findViewById(R.id.xlistview_footer_content);
            mProgressBar = moreView.findViewById(R.id.xlistview_footer_progressbar);
            mHintView = (TextView)moreView.findViewById(R.id.xlistview_footer_hint_textview);
//            mHintView.setText("load more");
//            mProgressBar.setVisibility(VISIBLE);//一直会显示转圈，自动加载更多时使用  
        }

    }
  
    /** 
     * 为了防止代码上的混乱，使用这个recyclerView自己内置的一个adapter 
     * @param <T> 
     */  
    public static abstract class XRecyclerViewAdapter<T> extends RecyclerView.Adapter<ViewHolder>{
        protected static final int TYPE_HEADER = 436874;  
        protected static final int TYPE_ITEM = 256478;  
        protected static final int TYPE_FOOTER = 9621147;  
  
        private int ITEM;  
  
        private ViewHolder vhItem;  
        protected boolean loadMore;  
  
        private List<T> dataList;  
  
        public List<T> getDataList() {  
            return dataList;  
        }  
  
        public void setDataList(List<T> dataList) {  
            this.dataList = dataList;  
        }  
  
        public XRecyclerViewAdapter(List<T> dataList, int itemLayout, boolean pullEnable){
            this.dataList = dataList;  
            this.ITEM = itemLayout;  
            this.loadMore = pullEnable;  
        }  
  
        public abstract ViewHolder setItemViewHolder(View itemView);  
  
        private T getObject(int position){  
            if(dataList!=null && dataList.size()>=position)return dataList.get(position-1);//如果有header  
            return null;  
        }  
  
        @Override  
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {  
                //inflate your layout and pass it to view holder  
                View itemView = LayoutInflater.from(parent.getContext()).inflate(ITEM,null);  
                Log.i("Alex","itemView是"+itemView);  
                this.vhItem = setItemViewHolder(itemView);  
                Log.i("Alex","vhItem是"+vhItem);  
                return vhItem;  
            } else if (viewType == TYPE_HEADER) {  
                //inflate your layout and pass it to view holder  
                View headerView = new CustomDragHeaderView(parent.getContext());  
                return new VHHeader(headerView);  
            } else if(viewType==TYPE_FOOTER){  
                CustomDragFooterView footerView = new CustomDragFooterView(parent.getContext());
                return new VHFooter(footerView);  
            }  
  
            throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");  
        }  
  
        public void setPullLoadMoreEnable(boolean enable){  
            this.loadMore = enable;  
        }  
        public boolean getPullLoadMoreEnable(){return loadMore;}  
  
        @Override  
        public void onBindViewHolder(ViewHolder holder, int position) {//相当于getView
            Log.i("Alex","正在绑定"+position+"    "+holder.getClass());  
            if (vhItem!=null && holder.getClass() == vhItem.getClass()) {  
                //cast holder to VHItem and set data  
                initItemView(holder,position,getObject(position));  
            }else if (holder.getClass() == VHHeader.class) {
                //cast holder to VHHeader and set data for header.  

            }else if(holder.getClass() ==  VHFooter.class){
                if(!loadMore)((VHFooter)holder).footerView.hide();//第一次初始化显示的时候要不要显示footerView
            }
        }  
  
        @Override  
        public int getItemCount() {  
            return (dataList==null ||dataList.size()==0)?1:dataList.size() + 2;//如果有header,若list不存在或大小为0就没有footView，反之则有  
        }//这里要考虑到头尾部，多以要加2  
  
        /** 
         * 根据位置判断这里该用哪个ViewHolder 
         * @param position 
         * @return 
         */  
        @Override  
        public int getItemViewType(int position) {  
            if (position == 0)return TYPE_HEADER;  
            else if(isPositonFooter(position))return TYPE_FOOTER;  
            return TYPE_ITEM;  
        }  

        protected boolean isPositonFooter(int position){//这里的position从0算起  
                if (dataList == null && position == 1) return true;//如果没有item  
                return position == dataList.size() + 1;//如果有item(也许为0)  
        }

//        class VHItem extends RecyclerView.ViewHolder {
//            public VHItem(View itemView) {  
//                super(itemView);  
//            }  
//            public View getItemView(){return itemView;}  
//        }  
//  
        protected class VHHeader extends ViewHolder {
            public VHHeader(View headerView) {  
                super(headerView);  
            }  
        }  
  
        protected class VHFooter extends ViewHolder {
            public CustomDragFooterView footerView;
  
            public VHFooter(View itemView) {  
                super(itemView);  
                footerView = (CustomDragFooterView)itemView;
            }  
        }  
  
        public abstract void initItemView(ViewHolder itemHolder,int posion,T entity);  
  
    }  
  
  
} 