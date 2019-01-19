package com.readboy.bean;

import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.VideoColumns;

/**
 * @author oubin
 * @date 2019/1/14
 */
public class VideoInfo {

    public int id;
    public int duration;
    public String data;
    public String displayName;
    public String title;
    public String mimeType;
    public String miniThumbMagic;


    private VideoInfo() {

    }

    public static VideoInfo createVideoInfo(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        VideoInfo info = new VideoInfo();
        info.id = cursor.getInt(cursor.getColumnIndex(VideoColumns._ID));
        info.duration = cursor.getInt(cursor.getColumnIndex(VideoColumns.DURATION));
        info.data = cursor.getString(cursor.getColumnIndex(VideoColumns.DATA));
        info.displayName = cursor.getString(cursor.getColumnIndex(VideoColumns.DISPLAY_NAME));
        info.mimeType = cursor.getString(cursor.getColumnIndex(VideoColumns.MIME_TYPE));
        info.miniThumbMagic = cursor.getString(cursor.getColumnIndex(VideoColumns.MINI_THUMB_MAGIC));
        return info;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "id=" + id +
                ", duration=" + duration +
                ", data='" + data + '\'' +
                ", displayName='" + displayName + '\'' +
                ", title='" + title + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", miniThumbMagic='" + miniThumbMagic + '\'' +
                '}';
    }
}
