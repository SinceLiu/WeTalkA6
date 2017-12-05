package com.readboy.utils;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;
import com.readboy.bean.Constant;

public class GlideConfiguration implements GlideModule{

	private static final int MEMORY_MAX_SPACE = (int) (Runtime.getRuntime().maxMemory() / 16);
	
	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
        //设置加载图片的样式
        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
        builder.setMemoryCache(new LruResourceCache(MEMORY_MAX_SPACE));
        LogInfo.i("hwj","max cache size = " + MEMORY_MAX_SPACE);
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, Constant.getImagePath(context) + "cache/", 20*1024*1024));
	}

	@Override
	public void registerComponents(Context arg0, Glide arg1) {
		
	}

}
