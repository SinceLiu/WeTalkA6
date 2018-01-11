package com.readboy.wetalk;

import uk.co.senab.photoview.PhotoView;

import android.os.Bundle;

import com.readboy.utils.GlideImgManager;

public class DisplayImageActivity extends BaseActivity{
	
	private PhotoView mPhotoView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_image_layout);
		initView();
	}

	@Override
	protected void initView() {
		mPhotoView = (PhotoView) findViewById(R.id.display_photo_view);
		initData();
	}

	@Override
	protected void initData() {
		String url = getIntent().getStringExtra("url");
		GlideImgManager.displaySimpleImage(this, url, mPhotoView, GlideImgManager.NORMAL);
	}

}
