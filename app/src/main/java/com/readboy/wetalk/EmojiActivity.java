package com.readboy.wetalk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
//import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.readboy.bean.Constant;
import com.readboy.utils.EmojiUtils;

public class EmojiActivity extends BaseActivity {

	private RecyclerView mEmojiRecyclerView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_square_emoji);
		initView();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void initView() {
		mEmojiRecyclerView = (RecyclerView) getView(R.id.square_emoji_recycle_list);
		initData();
	}

	@Override
	protected void initData() {
		GridLayoutManager layoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
		mEmojiRecyclerView.setLayoutManager(layoutManager);
//		new LinearSnapHelper().attachToRecyclerView(mEmojiRecyclerView);
		mEmojiRecyclerView.setAdapter(new EmojiRecyclerAdapter());
	}
	
	public class EmojiRecyclerAdapter extends RecyclerView.Adapter<EmojiRecyclerAdapter.ViewHolder>{
		
		class ViewHolder extends RecyclerView.ViewHolder{

			ImageView emoji;
			
			public ViewHolder(View itemView) {
				super(itemView);
				emoji = (ImageView) itemView.findViewById(R.id.emoji_content);
			}
			
		}

		@Override
		public int getItemCount() {
			return EmojiUtils.ALL_EMOJI_SINGLE_ID.length;
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, final int position) {
			Glide.with(EmojiActivity.this)
				.load(EmojiUtils.ALL_EMOJI_SINGLE_ID[position])
				.dontAnimate()
				.into(holder.emoji);
			holder.emoji.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.setScaleX(0.95f);
						v.setScaleY(0.95f);
						break;
					case MotionEvent.ACTION_UP:
						v.setScaleX(1.0f);
						v.setScaleY(1.0f);
						Intent intent = new Intent(EmojiActivity.this,ConversationActivity.class);
				        intent.putExtra(Constant.REQUEST_EMOJI_ID,EmojiUtils.ALL_EMOJI_SINGLE_ID[position]);
				        setResult(RESULT_OK,intent);
				        finish();	
						break;
					case MotionEvent.ACTION_CANCEL:
						v.setScaleX(1.0f);
						v.setScaleY(1.0f);
						break;
					default:
						break;
					}
					return true;
				}
			});
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
			return new ViewHolder(getLayoutInflater().inflate(R.layout.emoji_square_item, parent, false));
		}
	}
}
