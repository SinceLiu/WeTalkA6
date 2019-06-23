package com.readboy.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;

import com.readboy.bean.VideoInfo;

import java.io.File;

/**
 * @author oubin
 * @date 2019/1/12
 */
public class MediaUtils {
    private static final String TAG = "hwj_MediaUtils";

    public static Bitmap getVideoPhoto(String videoPath) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(videoPath);
        Bitmap bitmap = media.getFrameAtTime();
        media.release();
        return bitmap;
    }

    /**
     * 获取视频总时长
     *
     * @param path 路径
     * @return 单位毫秒
     */
    public static String getVideoDuration(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        retriever.release();
        return time;
    }

    public static int[] getVideoSize(String path) {
        if (!new File(path).exists()) {
            return null;
        }
        int[] result = new int[2];
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        try {
            result[0] = Integer.valueOf(width);
            result[1] = Integer.valueOf(height);
        } catch (Exception e) {
            Log.i(TAG, "getVideoSize: " + e.toString());
        }
        retriever.release();
        return result;
    }

    public static void getVideo(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        retriever.release();
        Log.i(TAG, "getVideo: width = " + width + ", height = " + height);
    }

    public static String getImagePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            try {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        if (index > -1) {
                            data = cursor.getString(index);
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.w(TAG, "getImagePath: e = " + e.toString(), e);
                return "";
            }
        }
        return data;
    }

    public static VideoInfo getVideoInfo(Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null,
                    null, null)) {
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        VideoInfo info = VideoInfo.createVideoInfo(cursor);
//                        if (info != null && (info.width <= 0 || info.height <= 0)) {
//                            try {
//                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//                                retriever.setDataSource(context, uri);
//                                info.width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
//                                info.height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
//                                retriever.release();
//                            } catch (Exception e) {
//                                Log.e(TAG, "getVideoInfo: e = " + e.toString());
//                            }
//                        }
                        return info;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "getRealFilePath: e = " + e.toString(), e);
                return null;
            }
        }
        return null;
    }

}
