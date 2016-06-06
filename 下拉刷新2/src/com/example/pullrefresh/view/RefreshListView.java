package com.example.pullrefresh.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.pullrefresh.R;

public class RefreshListView extends ListView implements OnScrollListener {

	private ProgressBar pb_rotate;;
	private ImageView iv_arrow;
	private TextView tv_state;
	private TextView tv_time;

	private int headViewHeight; // headview的高
	private int downY;// 按下y坐标

	private final int PULL_REFRESH = 0; // 下拉刷新状态
	private final int RELESA_REFRESH = 1; // 松开刷新状态
	private final int REFRESHING = 2; // 正在刷新的状态
	private int currentState = PULL_REFRESH;

	private View headerView;
	private RotateAnimation upanimation;
	private RotateAnimation downAnimation;
	private View footerView;
	private int footerViewHeight;

	private boolean isLoadingMore = false;// 当前是否正在处于加载更多

	public RefreshListView(Context context) {
		super(context);
		init();
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setOnScrollListener(this);
		initHeadView();
		initAnomation();
		initFooterView();
	}

	/**
	 * 初始化headView
	 */
	private void initHeadView() {

		headerView = View.inflate(getContext(), R.layout.layout_header, null);

		iv_arrow = (ImageView) headerView.findViewById(R.id.iv_arrow);
		pb_rotate = (ProgressBar) headerView.findViewById(R.id.pb_rotate);
		tv_state = (TextView) headerView.findViewById(R.id.tv_state);
		tv_time = (TextView) headerView.findViewById(R.id.tv_time);

		headerView.measure(0, 0);// 主动通知系统去测量该view
		headViewHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -headViewHeight, 0, 0);

		addHeaderView(headerView);
	}

	private void initFooterView() {

		footerView = View.inflate(getContext(), R.layout.layout_footer, null);
		footerView.measure(0, 0);// 主动通知系统去测量该view;
		footerViewHeight = footerView.getMeasuredHeight();
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		addFooterView(footerView);
	}

	/**
	 * 初始化旋转动画
	 */
	private void initAnomation() {
		upanimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		upanimation.setDuration(300);
		upanimation.setFillAfter(true);

		downAnimation = new RotateAnimation(-180, -360,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		downAnimation.setDuration(300);
		downAnimation.setFillAfter(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = (int) ev.getY();
			break;

		case MotionEvent.ACTION_MOVE:
			if (currentState == REFRESHING) {
				break;
			}
			int deltaY = (int) (ev.getY() - downY);

			int paddingTop = -headViewHeight + deltaY;
			if (paddingTop > -headViewHeight && getFirstVisiblePosition() == 0) {
				headerView.setPadding(0, paddingTop, 0, 0);
				// 从下拉刷新进入松开刷新状态
				if (paddingTop > 0 && currentState == PULL_REFRESH) {
					currentState = RELESA_REFRESH;
					refreshHeaderView();
				} else if (paddingTop < 0 && currentState == RELESA_REFRESH) {
					// 进入下拉刷新状态
					currentState = PULL_REFRESH;
					refreshHeaderView();
				}
				return true;// 拦截TouchMove，不让listview处理该次move事件,会造成listview无法滑动
			}
			break;
		case MotionEvent.ACTION_UP:
			if (currentState == PULL_REFRESH) {
				// 隐藏headerview
				headerView.setPadding(0, -headViewHeight, 0, 0);
			} else if (currentState == RELESA_REFRESH) {
				headerView.setPadding(0, 0, 0, 0);
				currentState = REFRESHING;
				refreshHeaderView();
				if (listener != null) {
					listener.onPullRefresh();
				}
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	private void refreshHeaderView() {
		switch (currentState) {
		case PULL_REFRESH:
			tv_state.setText("下拉刷新");
			iv_arrow.startAnimation(downAnimation);
			break;
		case RELESA_REFRESH:
			tv_state.setText("松开刷新");
			iv_arrow.startAnimation(upanimation);
			break;
		case REFRESHING:
			iv_arrow.clearAnimation();
			iv_arrow.setVisibility(View.INVISIBLE);
			pb_rotate.setVisibility(View.VISIBLE);
			tv_state.setText("正在刷新...");
			break;
		}
	}

	/**
	 * 完成刷新操作，重置状态,在你获取完数据并更新完adater之后，去在UI线程中调用该方法
	 */
	public void completeRefresh() {
		if (isLoadingMore) {
			// 重置footerView状态
			footerView.setPadding(0, -footerViewHeight, 0, 0);
			isLoadingMore = false;
		} else {
			headerView.setPadding(0, -headViewHeight, 0, 0);
			currentState = PULL_REFRESH;
			pb_rotate.setVisibility(View.INVISIBLE);
			iv_arrow.setVisibility(View.VISIBLE);
			tv_state.setText("下拉刷新");
			tv_time.setText("最后属性：" + getCurrentTime());
		}
	}

	/**
	 * 获取当前系统时间，并格式化
	 * 
	 * @return
	 */
	private String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		return format.format(new Date());

	}

	private OnRefreshListener listener;

	public void setOnRefreshListener(OnRefreshListener listener) {
		this.listener = listener;
	}

	public interface OnRefreshListener {
		void onPullRefresh();

		void onLoadingMore();
	}

	/**
	 * SCROLL_STATE_IDLE:闲置状态，就是手指松开 SCROLL_STATE_TOUCH_SCROLL：手指触摸滑动，就是按着来滑动
	 * SCROLL_STATE_FLING：快速滑动后松开
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& getLastVisiblePosition() == (getCount() - 1)) {
			isLoadingMore = true;
			footerView.setPadding(0, 0, 0, 0);// 显示出footerView

			setSelection(getCount());// 让listview最后一条显示出来
			if (listener != null) {
				listener.onLoadingMore();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}
}
