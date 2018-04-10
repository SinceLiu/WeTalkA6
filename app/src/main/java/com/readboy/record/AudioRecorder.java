package com.readboy.record;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

import com.readboy.bean.Constant;
import com.readboy.utils.LogInfo;
import com.readboy.utils.NetWorkUtils;

import java.io.File;
import java.io.IOException;

/**
 * 	录音类的相关封装
 *
 */
public class AudioRecorder implements RecordStrategy {
	private static final String TAG = "hwj_AudioRecorder";

	private MediaRecorder recorder;
	private String fileName;
	private boolean isRecording = false;
	private Context mContext;
	
	public AudioRecorder(Context context) {
		mContext = context;
	}

	@Override
	public void ready() {
		File file = new File(Constant.getVoicePath(mContext));
		if (!file.exists()) {
			boolean result = file.mkdirs();
		}
		fileName = NetWorkUtils.md5(String.valueOf(System.currentTimeMillis()));
		recorder = new MediaRecorder();
		recorder.setOutputFile(Constant.getVoicePath(mContext) + fileName);
		// 设置MediaRecorder的音频源为麦克风
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// 设置MediaRecorder录制的音频格式
		recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
		// 设置MediaRecorder录制音频的编码为amr
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				Log.e(TAG, "onError() called with: mr = " + mr + ", what = " + what + ", extra = " + extra + "");
			}
		});
	}

	@Override
	public void start() {
		if (!isRecording) {
			try {
				recorder.prepare();
				recorder.start();
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
			isRecording = true;
		}
	}

	@Override
	public void stop() {
		if (isRecording) {
			try {
				recorder.setOnErrorListener(null);  
				recorder.setOnInfoListener(null);    
				recorder.setPreviewDisplay(null);  
				recorder.stop();
				recorder.release();
				recorder = null;
				isRecording = false;
			}catch (IllegalStateException e) {  
                // TODO: handle exception  
                LogInfo.i("Exception", Log.getStackTraceString(e));  
            }catch (RuntimeException e) {  
                // TODO: handle exception  
            	LogInfo.i("Exception", Log.getStackTraceString(e));  
            }catch (Exception e) {  
                // TODO: handle exception  
            	LogInfo.i("Exception", Log.getStackTraceString(e));  
            }  
		}
	}

	@Override
	public void deleteOldFile() {
		File file = new File(Constant.getVoicePath(mContext) + fileName);
		file.deleteOnExit();
	}

	@Override
	public double getAmplitude() {
		if (!isRecording) {
			return 0;
		}
		try{
            return recorder.getMaxAmplitude();
		}catch(Exception e){
            return 0;
		}
	}

	@Override
	public String getFilePath() {
		return Constant.getVoicePath(mContext) + fileName;
	}

}
