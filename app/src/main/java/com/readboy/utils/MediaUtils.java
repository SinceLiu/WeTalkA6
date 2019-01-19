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

    public static void getVideo(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        String path = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
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

    /**
     * @return
     */
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
            String[] projection = new String[]{
                    VideoColumns.DATA,
                    VideoColumns.DURATION,
                    VideoColumns.MINI_THUMB_MAGIC
            };
            try (Cursor cursor = context.getContentResolver().query(uri, null, null,
                    null, null)) {
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        String[] temp1 = cursor.getColumnNames();
                        return VideoInfo.createVideoInfo(cursor);
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
