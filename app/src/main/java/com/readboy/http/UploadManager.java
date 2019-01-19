package com.readboy.http;

/**
 *
 * @author oubin
 * @date 2019/1/12
 */
public class UploadManager {

    /**
     * 上传到阿里服务器。
     * 正式服务器
     */
    private static final String HOST = "http://wear.readboy.com";
    private static final String UPLOAD_AVATAR_URL = HOST + "/put/avatar";
    private static final String UPLOAD_IMAGE_URL = HOST + "/put/image";
    private static final String UPLOAD_AUDIO_URL = HOST + "/put/audio";
    private static final String UPLOAD_VIDEO_URL = HOST + "/put/video";

    private UploadManager() {}

    public static void upload(){

    }
}
