package com.readboy.wetalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.readboy.bean.Constant;
import com.readboy.utils.IOs;
import com.readboy.utils.NetWorkUtils;

public class GetImageActivity extends BaseActivity{

	private RecyclerView mImageList;
	
	private List<String> mImagePaths;
	
	private View mLoading;
	
	private String cameraPath = "";
	
	//监拍图片路径
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_image);
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
	protected void initView() {
		mImageList = (RecyclerView) getView(R.id.choose_image_list);
		GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
		mImageList.setLayoutManager(layoutManager);
		mLoading = getView(R.id.loading);
		mImagePaths = new ArrayList<>();
		initData();
	}

	@Override
	protected void initData() {
		new GetAllCameraPhotoTask().execute();
	}
	
	/**
     * 隐藏加载中动画
     */
    private void hideLoading(){
        Animator hide = ObjectAnimator.ofFloat(mLoading,"alpha",0);
        hide.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                mLoading.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mLoading.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                mLoading.setVisibility(View.GONE);
            }
        });
        hide.start();
    }

    /**
     * 调用系统相机拍照
     */
    private void getCameraImage(){
        //调用系统相机的Intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //要存储照片的根目录
        File filePath = new File(Constant.getImagePath(this));
        if(!filePath.exists()){
            //有s的是创建多级目录
            boolean result = filePath.mkdirs();
            if(!result){
                showMsg(getString(R.string.disk_error));
            }
        }
        //照片的本地真实路径
        cameraPath = Constant.getImagePath(this) +
                NetWorkUtils.md5(String.valueOf(System.currentTimeMillis()));
        //传递跳转参数,照片数据要存到自定义的文件内(要控制照片质量)
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(MediaStore.Images.Media.DATA, cameraPath);
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(cameraIntent, Constant.REQUEST_CAMERA);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode != RESULT_OK){
    		return;
    	}
    	if(requestCode == Constant.REQUEST_CAMERA){
    		if(new File(cameraPath).length() == 0){
                showMsg(getString(R.string.get_image_fail));
                return;
            }
            fixImageQuality(cameraPath);
    	}
    }
    
    //处理保存图片的结果
    private Handler mSavePicHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == Constant.IMAGE_DONE){
            	Intent intent = new Intent(GetImageActivity.this,ConversationActivity.class);
            	intent.putExtra("path", (String) msg.obj);
            	setResult(RESULT_OK,intent);
            	finish();
            }
        }
    };
    
    /**
     * 调整相机的拍照质量
     */
    private void fixImageQuality(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        IOs.savePicInLocal(bitmap,NetWorkUtils.md5(String.valueOf(System.currentTimeMillis()))
        		,Constant.getImagePath(this),mSavePicHandler,100);
    }
    
	private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{

        private final String TYPE_CAMERA = "camera";
        private final String TYPE_PICTURE = "picture";

		static final int CAMERA = 0;
		static final int PICTURE = 1;
		
		class ViewHolder extends RecyclerView.ViewHolder{

			private View item;
			private ImageView content;
			
			public ViewHolder(View itemView) {
				super(itemView);
				content = (ImageView) itemView.findViewById(R.id.image_content);
				item = itemView.findViewById(R.id.item_image);
			}
		}

		@Override
		public int getItemViewType(int position) {
			if(TYPE_CAMERA.equals(mImagePaths.get(position))){
				return CAMERA;
			}else{
				return PICTURE;
			}
		}
		
		@Override
		public int getItemCount() {
			return mImagePaths.size();
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, final int position) {
			switch (getItemViewType(position)) {
			case CAMERA:
				holder.item.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(final View v, MotionEvent event) {
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							v.setScaleX(0.95f);
							v.setScaleY(0.95f);
							break;
						case MotionEvent.ACTION_UP:
							v.setScaleX(1.0f);
							v.setScaleY(1.0f);
							break;
						case MotionEvent.ACTION_CANCEL:
							v.setScaleX(1.0f);
							v.setScaleY(1.0f);
							break;
						default:
							break;
						}
						return false;
					}
				});
				holder.item.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(final View v) {
						v.setEnabled(false);	
						getCameraImage();
						new Handler().postDelayed(new Runnable() {
							
							@Override
							public void run() {
								v.setEnabled(true);
							}
						}, 1000);
					}
				});
				holder.content.setImageResource(R.drawable.camera_icon);
				break;
			case PICTURE:
				holder.item.setOnTouchListener(new OnTouchListener() {
					
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
							break;
						case MotionEvent.ACTION_CANCEL:
							v.setScaleX(1.0f);
							v.setScaleY(1.0f);
							break;
						default:
							break;
						}
						return false;
					}
				});
				holder.item.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(final View v) {
						v.setEnabled(false);	
						if(position < mImagePaths.size()){
							fixImageQuality(mImagePaths.get(position));
						}	
						new Handler().postDelayed(new Runnable() {
							
							@Override
							public void run() {
								v.setEnabled(true);
							}
						}, 1000);
					}
				});
				Glide.with(GetImageActivity.this)
					.load(mImagePaths.get(position))
					.placeholder(R.drawable.pic_holder)
            		.error(R.drawable.error)
            		.dontAnimate()
            		.centerCrop()
					.into(holder.content);
				break;
			default:
				break;
			}
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
			return new ViewHolder(getLayoutInflater().inflate(R.layout.item_image, parent,false));
		}
	}
	
	private class GetAllCameraPhotoTask extends AsyncTask<Void, Void, List<String>>{

		@Override
		protected List<String> doInBackground(Void... params) {
			String path = "/storage/emulated/0/DCIM/Camera/";
			File file = new File(path);
			List<String> paths = new ArrayList<>();
			if(file.exists()){
				File[] allPhoto = file.listFiles();
				bubbleSort(allPhoto);
				for (File f : allPhoto) {
					paths.add(f.getPath());
				}
			}
			return paths;
		}
		
		@Override
		protected void onPostExecute(List<String> result) {
			hideLoading();
			mImagePaths = result;
			mImagePaths.add(0,"camera");
			mImageList.setAdapter(new ImageAdapter());
		}
	}
	
	/**
	 * 按时间排序
	 */
	public void bubbleSort(File[] files) {   
	    int n = files.length;   
	    for (int i = 0; i < n - 1; i++) {   
	      for (int j = 0; j < n - 1; j++) {   
	        if (files[j].lastModified() < files[j + 1].lastModified()) {   
	          File temp = files[j];   
	          files[j] = files[j + 1];   
	          files[j + 1] = temp;   
	        }   
	      }   
	    }   
	}   
	
}
