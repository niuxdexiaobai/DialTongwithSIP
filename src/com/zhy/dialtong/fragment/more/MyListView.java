package com.zhy.dialtong.fragment.more;

import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhy.dialtong.R;

public class MyListView extends ListView implements OnScrollListener{
	
	private static final String TAG = "listview";

	/** �������̵�״ֵ̬����header�߶ȴ���һ����ʱ��Ϊ���״̬ */
	private final static int RELEASE_To_REFRESH = 0;
	/** ���������ص���ˢ�µ�״ֵ̬,��header�߶�С��һ����ʱ��Ϊ���״̬ */
	private final static int PULL_To_REFRESH = 1;
	/** ����ˢ�µ�״ֵ̬ */
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;

	/** ʵ�ʵ�padding�ľ����������ƫ�ƾ���ı���,eg:��ָ����Ļ�ϻ�����3����λ��header�ľ����ƶ���1����λ */
	private final static int RATIO = 3;

	private LayoutInflater inflater;

	// ListViewͷ������ˢ�µĲ���
	private LinearLayout headView;
	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;

	// ����ͷ������ˢ�µĲ��ֵĸ߶�
	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	/** ���ڱ�֤startY��ֵ��һ��������touch�¼���ֻ����¼һ�� */
	private boolean isRecored;

	private int headContentWidth;
	private int headContentHeight;

	/** �����Ļ�Ŀ�ʼλ�� */
	private int startY;
	/** ListView �ĵ�һ��Item��λ�� */
	private int firstItemIndex;

	private int state;

	private boolean isBack;

	private OnRefreshListener refreshListener;

	private boolean isRefreshable;

	public MyListView(Context context) {
		super(context);
		init(context);
	}

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		setCacheColorHint(context.getResources().getColor(
				android.R.color.transparent));
		inflater = LayoutInflater.from(context);

		headView = (LinearLayout) inflater.inflate(R.layout.head, null);

		arrowImageView = (ImageView) headView
				.findViewById(R.id.head_arrowImageView);
		// ��������ˢ��ͼ�����С�߶ȺͿ��
		arrowImageView.setMinimumWidth(70);
		arrowImageView.setMinimumHeight(50);
		progressBar = (ProgressBar) headView
				.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView);

		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();// 80
		headContentWidth = headView.getMeasuredWidth();// 220
		// �����ڱ߾࣬���þ��붥��Ϊһ�������������ֵĸ߶ȣ����ð�ͷ������
		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		// �ػ�
		headView.invalidate();

		Log.v("size", "width:" + headContentWidth + " height:"
				+ headContentHeight);
		// headView.setVisibility(View.INVISIBLE);
		// ������ˢ�µĲ��ּ���ListView�Ķ���
		addHeaderView(headView, null, false);
		// ���ù��������¼�
		setOnScrollListener(this);

		// ������ת�����¼�
		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);

		// һ��ʼ��״̬��������ˢ�����״̬������ΪDONE
		state = DONE;
		// �Ƿ�����ˢ��
		isRefreshable = false;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		firstItemIndex = firstVisibleItem;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	public boolean onTouchEvent(MotionEvent event) {

		if (isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (firstItemIndex == 0 && !isRecored) {
					isRecored = true;
					startY = (int) event.getY();// ��ָ����ʱ��¼��ǰλ��
					Log.v(TAG, "��downʱ���¼��ǰλ�á�" + startY);
				}
				break;

			// ��ָ�뿪��Ļ��ʱ���ж���ˢ�»��ǲ�ˢ��
			case MotionEvent.ACTION_UP:

				if (state != REFRESHING /* && state != LOADING */) {
					if (state == DONE) {
						// ʲô������
					}
					// ��ˢ�£�û����������
					if (state == PULL_To_REFRESH) {
						state = DONE;
						changeHeaderViewByState();
						Log.v(TAG, "������ˢ��״̬����done״̬");
					}
					// ˢ�£��û�����������
					if (state == RELEASE_To_REFRESH) {
						state = REFRESHING;
						changeHeaderViewByState();
						onRefresh();
						Log.v(TAG, "���ɿ�ˢ��״̬����done״̬");
					}
				}

				isRecored = false;
				isBack = false;

				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!isRecored && firstItemIndex == 0) {
					Log.v(TAG, "��moveʱ���¼��λ��" + tempY);
					isRecored = true;
					startY = tempY;
				}

				if (state != REFRESHING && isRecored /* && state != LOADING */) {

					// ��֤������padding�Ĺ����У���ǰ��λ��һֱ����head������������б�����Ļ�Ļ����������Ƶ�ʱ���б��ͬʱ���й���
					// ��������ȥˢ����
					if (state == RELEASE_To_REFRESH) {

						setSelection(0);

						// ��������û�дﵽˢ�µ�״̬����ʾ����ˢ�£�state Ϊ PULL_To_REFRESH
						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();

							Log.w(TAG, "���ɿ�ˢ��״̬ת�䵽����ˢ��״̬AAAAAAAAAAAAA");
						}
						// һ�����Ƶ�����
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							Log.v(TAG, "���ɿ�ˢ��״̬ת�䵽done״̬");
						}
						// �������ˣ����߻�û�����Ƶ���Ļ�����ڸ�head�ĵز�
						else {
							// ���ý����ر�Ĳ�����ֻ�ø���paddingTop��ֵ������
						}
					}
					// ��û�е�����ʾ�ɿ�ˢ�µ�ʱ��,DONE������PULL_To_REFRESH״̬
					if (state == PULL_To_REFRESH) {

						setSelection(0);

						// ���������Խ���RELEASE_TO_REFRESH��״̬
						if ((tempY - startY) / RATIO >= headContentHeight) {
							state = RELEASE_To_REFRESH;
							isBack = true;
							changeHeaderViewByState();

							Log.v(TAG, "��done��������ˢ��״̬ת�䵽�ɿ�ˢ��");
						}
						// ���Ƶ�����
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							Log.v(TAG, "��DOne��������ˢ��״̬ת�䵽done״̬");
						}
					}

					// done״̬��
					if (state == DONE) {
						if (tempY - startY > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}

					// ����headView��size
					if (state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);

					}

					// ����headView��paddingTop
					if (state == RELEASE_To_REFRESH) {
						headView.setPadding(0, (tempY - startY) / RATIO
								- headContentHeight, 0, 0);
					}

				}

				break;
			}
		}

		return super.onTouchEvent(event);
	}

	// ��״̬�ı�ʱ�򣬵��ø÷������Ը��½���
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);

			tipsTextview.setText("�ɿ�ˢ��");

			Log.v(TAG, "��ǰ״̬���ɿ�ˢ��");
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// ����RELEASE_To_REFRESH״̬ת������
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText("����ˢ��");
			} else {
				tipsTextview.setText("����ˢ��");
			}
			Log.v(TAG, "��ǰ״̬������ˢ��");
			break;

		case REFRESHING:

			headView.setPadding(0, 0, 0, 0);

			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText("����ˢ��...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.GONE);

			Log.v(TAG, "��ǰ״̬,����ˢ��...");
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);

			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.ic_pulltorefresh_arrow);
			tipsTextview.setText("����ˢ��");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.v(TAG, "��ǰ״̬��done");
			break;
		}
	}

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	/**
	 * Function:Important!!ʹ��ListView����ص����������֪ͨListView������ɣ��ı�״̬Ϊ
	 * {@link MyListView#REFRESHING}-> {@linkplain MyListView#DONE}
	 * ,��Ȼ��һ�ε�ˢ�»�����쳣<br>
	 * 
	 * @author ZYT DateTime 2014-4-22 ����1:45:34<br>
	 * <br>
	 */
	public void onRefreshComplete() {
		state = DONE;
		lastUpdatedTextView.setText("�������:" + new Date().toLocaleString());
		changeHeaderViewByState();
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}

	/**
	 * �˷���ֱ���հ��������ϵ�һ������ˢ�µ�demo���˴��ǡ����ơ�headView��width�Լ�height
	 * 
	 * @author zyt
	 */
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setAdapter(BaseAdapter adapter) {
		lastUpdatedTextView.setText("�������:" + new Date().toLocaleString());
		super.setAdapter(adapter);
	}

}
