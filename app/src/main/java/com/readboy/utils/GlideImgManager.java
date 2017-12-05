package com.readboy.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.readboy.wetalk.R;

public class GlideImgManager {
	
	public static final int ROUND = 0;
	public static final int CIRCLE = 1;
	public static final int NORMAL = 2;

	
	/**
	* load normal  for  circle or round img
	*
	* @param url
	* @param erroImg
	* @param emptyImg
	* @param iv
	* @param tag
	*/
	public static void displayImage(Context context, String url, ImageView iv, int tag) {
	    if (CIRCLE == tag) {
	        Glide.with(context)
		        .load(url)
		        .placeholder(R.drawable.pic_holder)
		        .error(R.drawable.error)
		        .transform(new GlideCircleTransform(context))
		        .into(iv);
	    } else if (ROUND == tag) {
	        Glide.with(context)
		        .load(url)
		        .placeholder(R.drawable.pic_holder)
		        .error(R.drawable.error)
		        .transform(new GlideRoundTransform(context,10))
		        .into(iv);
	    } else if(NORMAL == tag){
	    	Glide.with(context)
		    	.load(url)
		    	.placeholder(R.drawable.pic_holder)
		        .error(R.drawable.error)
		    	.into(iv);
	    }
	}
	
	public static void displayImage(Context context, int resId, ImageView iv, int tag) {
	    if (CIRCLE == tag) {
	        Glide.with(context)
		        .load(resId)
		        .error(R.drawable.error)
		        .transform(new GlideCircleTransform(context))
		        .into(iv);
	    } else if (ROUND == tag) {
	        Glide.with(context)
		        .load(resId)
		        .error(R.drawable.error)
		        .transform(new GlideRoundTransform(context,10))
		        .into(iv);
	    } else if(NORMAL == tag){
	    	Glide.with(context)
		    	.load(resId)
		        .error(R.drawable.error)
		    	.into(iv);
	    }
	}

	
	/**
	* load normal  for  circle or round img
	*
	* @param url
	* @param erroImg
	* @param emptyImg
	* @param iv
	* @param tag
	*/
	public static void displaySimpleImage(Context context, String url, ImageView iv, int tag) {
	    if (CIRCLE == tag) {
	        Glide.with(context)
		        .load(url)
		        .dontAnimate()
		        .error(R.drawable.error)
		        .transform(new GlideCircleTransform(context))
		        .into(iv);
	    } else if (ROUND == tag) {
	        Glide.with(context)
		        .load(url)
		        .dontAnimate()
		        .error(R.drawable.error)
		        .transform(new GlideRoundTransform(context,10))
		        .into(iv);
	    } else if(NORMAL == tag){
	    	Glide.with(context)
		    	.load(url)
		    	.dontAnimate()
		        .error(R.drawable.error)
		    	.into(iv);
	    }
	}
}
