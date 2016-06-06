package com.example.pullrefresh;

import java.util.ArrayList;
import com.example.pullrefresh.view.RefreshListView;
import com.example.pullrefresh.view.RefreshListView.OnRefreshListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {

	private RefreshListView refreshListView;

	private ArrayList<String> list = new ArrayList<String>();

	private MyAdapter adapter;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// 更新UI
			adapter.notifyDataSetChanged();
			refreshListView.completeRefresh();
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initView();
		initData();

	}

	private void initView() {
		refreshListView = (RefreshListView) findViewById(R.id.refreshListView);
	}

	private void initData() {
		for (int i = 0; i < 15; i++) {
			list.add("listview 原来的数据" + i);
		}

		adapter = new MyAdapter();
		refreshListView.setAdapter(adapter);

		refreshListView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onPullRefresh() {
				// 需要联网请求服务器的数据，然后更新UI
				requestDataFromSever(false);
			}

			@Override
			public void onLoadingMore() {
				// TODO Auto-generated method stub
				requestDataFromSever(true);
			}
		});
	}

	/**
	 * 模拟向服务器请求数据
	 * 
	 * @author Administrator
	 * 
	 */
	private void requestDataFromSever(final boolean isLoadingMore) {

		new Thread() {
			public void run() {
				SystemClock.sleep(3000);// 模拟请求服务器的一个时间长度

				if (isLoadingMore) {
					list.add("加载更多的数据-1");
					list.add("加载更多的数据-2");
					list.add("加载更多的数据-4");
					list.add("加载更多的数据-3");
				} else {
					list.add(0, "下拉刷新的数据+1");
				}
				// 在UI线程更新UI
				handler.sendEmptyMessage(0);
			};
		}.start();
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = new TextView(MainActivity.this);
			textView.setPadding(20, 20, 20, 20);
			textView.setTextSize(18);
			textView.setText(list.get(position));
			return textView;
		}

	}
}
