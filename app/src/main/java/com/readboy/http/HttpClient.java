package com.readboy.http;

import android.util.Log;

import com.readboy.bean.Constant;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author oubin
 * @date 2019/1/12
 */
public class HttpClient {
    private static final String TAG = "hwj_HttpClient";

    /**
     * 上传到阿里服务器。
     * 正式服务器
     */
    private static final String HOST = "http://wear.readboy.com";
    private static final String UPLOAD_AVATAR_URL = HOST + "/put/avatar";
    private static final String UPLOAD_IMAGE_URL = HOST + "/put/image";
    private static final String UPLOAD_AUDIO_URL = HOST + "/put/audio";
    private static final String UPLOAD_VIDEO_URL = HOST + "/put/video";
    private static final String PATH_IMAGE = "/put/image";
    private static final String PATH_AUDIO = "/put/audio";
    private static final String PATH_VIDEO = "/put/video";

    private static final String APPID = "appid";
    private static final String SN = "sn";
    private static final String MD5 = "md5";
    public static final String TIME = "t";
    public static final String IMAGE = "image";
    public static final String AUDIO = "audio";
    public static final String TEXT = "text";
    public static final String VIDEO = "video";
    public static final String LINK = "link";

    private static final String MEDIA_TYPE_IMAGE = "image/jpeg";
    private static final String MEDIA_TYPE_AUDIO = "audio/amr";
    private static final String MEDIA_TYPE_VIDEO = "video/mp4";
    private static final String MEDIA_TYPE_VIDEO2 = "video/mpeg";

    public static OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .build();
    }

    public static void uploadImage(String filePath, Callback callback) {
        File file = new File(filePath);
        if (!file.exists()) {
            callback.onFailure(null, new FileNotFoundException());
        }
        String filename = new File(filePath).getName();
        uploadFile(filePath, filename, FileUploader.IMAGE, callback);
    }

    public static void uploadVideo(String filePath, Callback callback) {
        if (!checkFile(filePath, callback)) {
            return;
        }
        String filename = new File(filePath).getName();
        uploadFile(filePath, filename, FileUploader.VIDEO, callback);
    }

    private static void uploadFile(String filePath, String fileName, FileUploader uploader, Callback callback) {
        Log.i(TAG, "uploadFile() called with: filePath = " + filePath + ", fileName = " + fileName + "");
        OkHttpClient client = getOkHttpClient();
        long t = System.currentTimeMillis();
        String sn = md5(Constant.PARAM_APPID + Constant.PARAM_SN + t);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(uploader.name, fileName,
                        RequestBody.create(MediaType.parse(uploader.mediaType), new File(filePath)))
                .build();

        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host("wear.readboy.com")
                .addPathSegment(uploader.path)
                .addQueryParameter(APPID, Constant.PARAM_APPID)
                .addQueryParameter(SN, sn)
                .addQueryParameter(TIME, String.valueOf(t))
                .build();
        Request request = new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 使用MD5算法进行加密
     */
    public static String md5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance(MD5).digest(plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有MD5这个算法!");
        }
        //16进制数, 如果第一位小于16，则md5code长度为31.
        String md5code = new BigInteger(1, secretBytes).toString(16);
        //如果生成数字没满32位,需要前面补0
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    private static boolean checkFile(String path, Callback callback) {
        File file = new File(path);
        if (!file.exists()) {
            callback.onFailure(null, new FileNotFoundException());
            return false;
        }
        return true;
    }

    public enum FileUploader {

        /**
         * mediaType需要根据具体情况，具体分析，如png图片，为image/png
         */
        IMAGE("/put/image", "image/jpeg", "image"),
        AUDIO("/put/audio", "audio/amr", "audio"),
        VIDEO("/put/video", "video/mp4", "video");
        String path;
        String mediaType;
        /**
         * 表单名
         */
        String name;

        FileUploader(String path, String mediaType, String name) {
            this.path = path;
            this.mediaType = mediaType;
            this.name = name;
        }

    }

}
