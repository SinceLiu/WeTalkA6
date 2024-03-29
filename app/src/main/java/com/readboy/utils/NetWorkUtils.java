package com.readboy.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.readboy.adapter.ConversationListAdapterSimple;
import com.readboy.bean.Constant;
import com.readboy.bean.Conversation;
import com.readboy.provider.ConversationProvider;
import com.readboy.provider.Conversations;
import com.readboy.wetalk.WeTalkApplication;

import android.app.readboy.ReadboyWearManager;
import android.app.readboy.IReadboyWearListener;
import android.util.Log;

import cz.msebera.android.httpclient.Header;

/**
 * @author 1-PC
 * @date 2016/9/24
 * 处理所有的网络请求
 */

public class NetWorkUtils {
    private static final String TAG = "hwj_NetWorkUtils";

    //服务器相关url
//    private static final String HOST = "http://120.25.120.222";//测试服务器
    /**
     * 上传到阿里服务器。
     * 正式服务器
     */
    private static final String HOST = "http://wear.readboy.com";
    private static final String UPLOAD_AVATAR_URL = HOST + "/put/avatar";
    private static final String UPLOAD_IMAGE_URL = HOST + "/put/image";
    private static final String UPLOAD_AUDIO_URL = HOST + "/put/audio";
    private static final String UPLOAD_VIDEO_URL = HOST + "/put/video";

    /**
     * 服务器响应Key
     */
    private static final String STATUS = "status";
    private static final String FILE_NAME = "filename";
    private static final String SRC_NAME = "srcname";

    /**
     * Json的字段
     */
    public static final String TYPE = "type";
    public static final String MESSAGE = "m";
    public static final String ATTR = "attr";
    public static final String SRC = "src";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String LENGTH = "length";
    public static final String HEADER = "h";
    public static final String DATA = "data";
    public static final String TIME = "t";
    private static final String APPID = "appid";
    private static final String SN = "sn";
    private static final String MD5 = "md5";
    public static final String IMAGE = "image";
    public static final String AUDIO = "audio";
    public static final String TEXT = "text";
    public static final String VIDEO = "video";
    public static final String LINK = "link";
    public static final String A = "a";
    public static final String SEND_MSG_ID = "sendmsgId";
    private static final String IMAGE_TYPE = "image/jpeg";
    private static final String AUDIO_TYPE = "audio/amr";
    private static final String VIDEO_TYPE = "video/mp4";
    public static final int UPLOAD_SUCCEED = 0x90;
    public static final int UPLOAD_FAIL = 0x71;

    public static final String CONVERSATION_TAG = "conversation";

    private static NetWorkUtils mNetWorkUtils;

    private NetWorkUtils(Context context) {
    }

    /**
     * 可能存在内存泄露
     *
     * @deprecated
     */
    public static synchronized NetWorkUtils getInstance(Context context) {
        if (mNetWorkUtils == null) {
            mNetWorkUtils = new NetWorkUtils(context);
        }
        return mNetWorkUtils;
    }

    public static ReadboyWearManager getManager(Context context) {
        return (ReadboyWearManager) context.getSystemService(Context.RBW_SERVICE);
    }

    /**
     * 检查网络状态
     *
     * @param context 上下文
     * @return 状态
     */
    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            return info != null && info.isAvailable() && info.isConnected();
        }
        return false;
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

    public static void uploadCaptureFile(Context context, final ArrayList<String> ids, String path, final Handler handler) {
        if (WeTalkApplication.IS_TEST_MODE) {
            return;
        }
        RequestParams params = getRequestParams();
        try {
            final File file = new File(path);
            if (!file.exists()) {
                return;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opts);
            final int width = opts.outWidth;
            final int height = opts.outHeight;
            params.put(IMAGE, file, IMAGE_TYPE);
            Log.i(TAG, "uploadCaptureFile: request = " + params.toString());
            getClient().post(UPLOAD_IMAGE_URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    LogInfo.i("hwj", "uploadFile file: onSuccess " + response);
                    int status = response.optInt(STATUS);
                    if (status == 200 && handler != null) {
                        String url = response.optString(SRC_NAME);
                        String thumbUrl = response.optString(FILE_NAME);
                        for (String id : ids) {
                            LogInfo.i("hwj", "receiver capture id : " + id);
                            sendCapture(context, url, thumbUrl, width, height, id, new PushResultListener() {

                                @Override
                                public void pushSucceed(String type, String s1, int code, String s,
                                                        String response) {
                                    handler.obtainMessage(UPLOAD_SUCCEED).sendToTarget();
                                    if (file.exists() && file.getName().contains("readboy_security")) {
                                        file.delete();
                                    }
                                }

                                @Override
                                public void pushFail(String s, String s1, int i, String s2) {
                                    if (handler != null) {
                                        handler.obtainMessage(UPLOAD_FAIL).sendToTarget();
                                        if (file.exists() && file.getName().contains("readboy_security")) {
                                            file.delete();
                                        }
                                    }
                                }
                            });
                        }
                    } else if (status != 200 && handler != null) {
                        handler.obtainMessage(UPLOAD_FAIL).sendToTarget();
                        if (file.exists() && file.getName().contains("readboy_security")) {
                            file.delete();
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    LogInfo.i("hwj", "uploadFile file: onFailure" + errorResponse);
                    if (handler != null) {
                        handler.obtainMessage(UPLOAD_FAIL).sendToTarget();
                        if (file.exists() && file.getName().contains("readboy_security")) {
                            file.delete();
                        }
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    LogInfo.i("hwj", "uploadFile file onFailure :" + responseString);
                    if (handler != null) {
                        handler.obtainMessage(UPLOAD_FAIL).sendToTarget();
                        if (file.exists() && file.getName().contains("readboy_security")) {
                            file.delete();
                        }
                    }
                }

                public void onFailure(Throwable throwable, JSONObject respon) {
                    LogInfo.i("hwj", "uploadFile file onFailure :" + respon);
                    if (handler != null) {
                        handler.obtainMessage(UPLOAD_FAIL).sendToTarget();
                        if (file.exists() && file.getName().contains("readboy_security")) {
                            file.delete();
                        }
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (handler != null) {
                handler.obtainMessage(UPLOAD_FAIL).sendToTarget();
            }
        }
    }

    /**
     * 上传文件(语音,图片)
     *
     * @param conversation 消息
     * @param handler      {@link ConversationListAdapterSimple#mReUploadFileHandler}
     */
    public static void uploadFile(final Conversation conversation, final Handler handler) {
        if (WeTalkApplication.IS_TEST_MODE) {
            return;
        }
        final int type = conversation.type;
        conversation.isSending = Constant.TRUE;
        RequestParams params = getRequestParams();
        switch (type) {
            //发送图片
            case Constant.SEND_IMAGE:
                uploadImage(conversation, handler, params);
                break;
            //发送语音
            case Constant.SEND_VOICE:
                uploadVoice(conversation, handler, params);
                break;
            case Constant.SEND_VIDEO:
                uploadVideo(conversation, handler, params);
                break;
            default:
                conversation.isSending = Constant.FALSE;
                Log.e(TAG, "uploadFile: default type = " + conversation.type);
        }
    }

    private static void uploadVoice(Conversation conversation, Handler handler, RequestParams params) {
        try {
            File audio = new File(conversation.voiceLocalPath);
            params.put(AUDIO, audio, AUDIO_TYPE);
            getClient().post(UPLOAD_AUDIO_URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    LogInfo.i("hwj", "uploadFile voice file: onSuccess " + response);
                    int status = response.optInt(STATUS);
                    if (status == 200 && handler != null) {
                        conversation.voiceUrl = response.optString(FILE_NAME);
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_SUCCEED));
                    } else if (status != 200 && handler != null) {
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.i(TAG, "uploadFile voice file onFailure() called with: statusCode = " + statusCode +
                            ", headers = " + headers + ", throwable = " + throwable +
                            ", errorResponse = " + errorResponse + "");
                    if (handler != null) {
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.i(TAG, "uploadFile onFailure() called with: statusCode = " + statusCode +
                            ", headers = " + headers + ", responseString = " + responseString +
                            ", throwable = " + throwable + "");
                    if (handler != null) {
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "uploadFile: e = " + e.toString());
            if (handler != null) {
                handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
            }
        }
    }

    private static void uploadImage(Conversation conversation, Handler handler, RequestParams params) {
        try {
            File image = new File(conversation.imageLocalPath);
            params.put(IMAGE, image, IMAGE_TYPE);
            Log.i(TAG, "uploadFile: params = " + params.toString());
            AsyncHttpClient client = getClient();
            client.setLoggingLevel(2);
            RequestHandle handle = client.post(UPLOAD_IMAGE_URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    for (Header header : headers) {
                        Log.i(TAG, "onSuccess: header " + header.getName() + ":" + header.getValue());
                    }
                    LogInfo.i("hwj", "uploadFile image file: onSuccess " + response);
                    int status = response.optInt(STATUS);
                    if (status == 200 && handler != null) {
                        conversation.imageUrl = response.optString(SRC_NAME);
                        conversation.thumbImageUrl = response.optString(FILE_NAME);
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_SUCCEED));
                    } else if (status != 200 && handler != null) {
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    LogInfo.i("hwj", "uploadFile image file: onFailure 1 :" + errorResponse);
                    if (handler != null) {
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    LogInfo.i("hwj", "uploadFile file onFailure 2 :" + responseString);
                    if (handler != null) {
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "uploadFile: e = " + e.toString());
            handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
        }
    }

    private static void uploadVideo(Conversation conversation, Handler handler, RequestParams params) {
        try {
            File audio = new File(conversation.voiceLocalPath);
            params.put(VIDEO, audio, VIDEO_TYPE);
            Log.i(TAG, "uploadVideo: request = " + params.toString());
            getClient().post(UPLOAD_VIDEO_URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    LogInfo.i("hwj", "uploadVideo video file: onSuccess " + response);
                    int status = response.optInt(STATUS);
                    if (status == 200 && handler != null) {
                        conversation.voiceUrl = response.optString(FILE_NAME);
                        conversation.extra = response.optString(FILE_NAME);
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_SUCCEED));
                    } else if (status != 200 && handler != null) {
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
                    }
                }

                @Override
                public void onProgress(long bytesWritten,long totalSize){
                    //TODO 显示进度
//                    Log.e("lxx","进度："+(int)(bytesWritten*1.0/totalSize*100)+"%");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.i(TAG, "uploadVideo voice file onFailure() called with: statusCode = " + statusCode +
                            ", headers = " + headers + ", throwable = " + throwable +
                            ", errorResponse = " + errorResponse + "");
                    if (handler != null) {
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.i(TAG, "uploadVideo onFailure() called with: statusCode = " + statusCode +
                            ", headers = " + Arrays.toString(headers) + ", responseString = " + responseString +
                            ", throwable = " + throwable + "");
                    if (handler != null) {
                        handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "uploadVideo: e = " + e.toString());
            if (handler != null) {
                handler.sendMessage(getUploadResultMessage(conversation, UPLOAD_FAIL));
            }
        }
    }

    protected static Message getUploadResultMessage(Conversation conversation, int result) {
        Message msg = new Message();
        msg.what = result;
        msg.arg1 = conversation.type;
        Bundle bundle = new Bundle();
        bundle.putParcelable(CONVERSATION_TAG, conversation);
        msg.setData(bundle);
        return msg;
    }

    /**
     * 请求的必须参数
     *
     * @return 基本请求封装
     */
    private static RequestParams getRequestParams() {
        RequestParams params = new RequestParams();
        long t = System.currentTimeMillis();
        String sn = md5(Constant.PARAM_APPID + Constant.PARAM_SN + t);
        params.put(APPID, Constant.PARAM_APPID);
        params.put(SN, sn);
        params.put(TIME, t);
        return params;
    }

    //指定语音文件类型
    private static String[] allowedContentTypes = new String[]{".*"};


    public interface DownLoadFileListener {
        void downLoadSucceed();

        void downLoadFailed();
    }

    /**
     * 下载语音、视频文件
     *
     */
    public static void downloadFile(final Context context, final Conversation conversation, final DownLoadFileListener downLoadFileListener) {
        if (WeTalkApplication.IS_TEST_MODE) {
            return;
        }
        String url = conversation.voiceUrl;
        LogInfo.i(TAG, "download voice url : " + url);
        Uri uri = Uri.parse(url);
//        String name = url.substring(url.indexOf("a/") + 2, url.length() - 4);
        String name = uri.getLastPathSegment();
        Log.i(TAG, "downloadFile: name = " + name);
        //下载完成,保存文件
        File dir = new File(Constant.getDownloadPath(context));
        if (!dir.exists() && !dir.mkdirs()) {
            Log.w(TAG, "downloadFile: create file fail. " + dir.getAbsolutePath());
            return;
        }
        final File file = new File(dir, name);
        //文件不存在才下载
        if (!file.exists()) {
            getClient().get(url, new BinaryHttpResponseHandler(allowedContentTypes) {
                @Override
                public void onSuccess(int status, Header[] headers, byte[] bytes) {
                    if (status == 200) {
                        try {
                            OutputStream outputStream = new FileOutputStream(file);
                            outputStream.write(bytes);
                            outputStream.flush();
                            outputStream.close();
                            conversation.voiceLocalPath = file.getPath();
                            if (downLoadFileListener != null) {
                                downLoadFileListener.downLoadSucceed();
                            }
                            Cursor cursor = context.getContentResolver().query(Conversations.Conversation.CONVERSATION_URI, null,
                                    Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conversation.conversationId},
                                    null);
                            //插入或更新数据库
                            if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
                                Uri uri = context.getContentResolver().insert(Conversations.Conversation.CONVERSATION_URI,
                                        ConversationProvider.getContentValue(conversation, true));
                                if (uri == null) {
                                    Log.i(TAG, "onSuccess: insert fail.");
                                }
                            } else {
                                ContentValues values = new ContentValues();
                                values.put(Conversations.Conversation.VOICE_PATH, conversation.voiceLocalPath);
                                int rows = context.getContentResolver().update(Conversations.Conversation.CONVERSATION_URI,
                                        values, Conversations.Conversation.CONVERSATION_ID + " = ?", new String[]{conversation.conversationId});
                                if (rows <= 0) {
                                    Log.i(TAG, "onSuccess: update fail.");
                                }
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.i(TAG, "onSuccess: e = " + e.toString());
                        }
                    } else {
                        Log.i(TAG, "onSuccess: status = " + status);
                        Log.i(TAG, "onSuccess: header = " + getHeadersString(headers));
                    }
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    Log.i(TAG, "onFailure: headers, " + getHeadersString(headers));
                    if (downLoadFileListener != null) {
                        downLoadFileListener.downLoadFailed();
                    }
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {

                }
            });
        }
    }

    private static AsyncHttpClient getClient() {
        if (Looper.myLooper() == null) {
            Log.e(TAG, "getClient: SyncHttpClient.");
            return new SyncHttpClient();
        } else {
            Log.e(TAG, "getClient: AsyncHttpClient.");
            return new AsyncHttpClient();
        }
    }

    public static String getDeviceUuid(Context context) {
        ReadboyWearManager manager = getManager(context);
        String uuid = "";
        if (manager != null && manager.getPersonalInfo() != null) {
            uuid = manager.getPersonalInfo().getUuid();
        }
        return uuid;
    }

    public static String getNickName(Context context) {
        ReadboyWearManager manager = getManager(context);
        String name = "";
        try {
            if (manager != null) {
                if (manager.getPersonalInfo() != null) {
                    name = manager.getPersonalInfo().getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public static void saveMessageTag(final Context context) {
        ReadboyWearManager manager = getManager(context);
        if (manager == null) {
            return;
        }
        if (TextUtils.isEmpty(MPrefs.getMessageTag(context))) {
            try {
                manager.getAllMessage(MPrefs.getMessageTag(context), new IReadboyWearListener.Stub() {
                    @Override
                    public void pushSuc(String type, String s1, int code, String s, String response) throws RemoteException {
                        //s = mget, s1 = 119, i = 0, s2 = [], s3 = {"r":"mget","o":"119","t":"18020100229007","data":[]}
                        try {
                            MPrefs.setMessageTag(context, new JSONObject(response).getString(NetWorkUtils.TIME));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void pushFail(String s, String s1, int i, String s2) throws RemoteException {

                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public static void getAllMessage(Context context, String tag, final PushResultListener listener) {
        ReadboyWearManager manager = getManager(context);
        try {
            manager.getAllMessage(tag, new IReadboyWearListener.Stub() {
                @Override
                public void pushSuc(String type, String s1, int code, String s, String response) throws RemoteException {
                    listener.pushSucceed(type, s1, code, s, response);
                }

                @Override
                public void pushFail(String s, String s1, int i, String s2) throws RemoteException {
                    listener.pushFail(s, s1, i, s2);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
            listener.pushFail("", "", 0, "");
        }
    }

    public interface PushResultListener {
        void pushSucceed(String type, String s1, int code, String s, String response);

        void pushFail(String s, String s1, int i, String s2);

    }

    /**
     * 发送监拍图片
     *
     * @param url      图片url
     * @param uuid     uuid
     * @param listener 回调
     */
    private static void sendCapture(Context context, String url, String thumbUrl, int width, int height, String uuid,
                                    final PushResultListener listener) {
        ReadboyWearManager manager = getManager(context);
        if (manager == null) {
            return;
        }
        LogInfo.i("hwj", "send capture:url = " + url + " thumb = " + thumbUrl);
        try {
            manager.putDeviceCapture(uuid, width, height, thumbUrl, url, new IReadboyWearListener.Stub() {
                @Override
                public void pushSuc(String type, String s1, int code, String s2, String response) throws RemoteException {
                    listener.pushSucceed(type, s1, code, s2, response);
                }

                @Override
                public void pushFail(String s, String s1, int i, String s2) throws RemoteException {
                    listener.pushFail(s, s1, i, s2);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
            listener.pushFail("", "", 0, "");
        }
    }

    /**
     * 通过CorePush发送信息给家长端
     */
    public static void sendMessage(Context context, Conversation conversation, final PushResultListener listener) {
        String recId = conversation.recId;
        String type = null;
        int length = 0;
        int width = 0;
        int height = 0;
        String thumbUrl = null;
        String url = null;
        switch (conversation.type) {
            case Constant.SEND_EMOJI:
                type = TEXT;
//                url = EmojiUtils.getEmojiCode(conversation.emojiId);
                if (TextUtils.isEmpty(conversation.emojiCode)) {
                    Log.e(TAG, "sendMessage: emojiCode = " + conversation.emojiCode);
                    url = EmojiUtils.getEmojiCode(conversation.emojiId);
                } else {
                    url = conversation.emojiCode;
                }
                break;
            case Constant.SEND_IMAGE:
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(conversation.imageLocalPath, opts);
                type = IMAGE;
                width = opts.outWidth;
                height = opts.outHeight;
                thumbUrl = conversation.thumbImageUrl;
                url = conversation.imageUrl;
                break;
            case Constant.SEND_VOICE:
                type = AUDIO;
                length = conversation.lastTime;
                url = conversation.voiceUrl;
                break;
            case Constant.SEND_VIDEO:
                type = VIDEO;
                length = conversation.lastTime;
                url = conversation.voiceUrl;
                int[] size = MediaUtils.getVideoSize(conversation.content);
                if (size != null && size.length >= 2) {
                    width = size[0];
                    height = size[1];
                }
                break;
            default:
                break;
        }
        ReadboyWearManager manager = getManager(context);
        if (manager == null) {
            return;
        }
        try {
            manager.sendChatMessage(conversation.recId, type, length, width, height, thumbUrl, url, new IReadboyWearListener.Stub() {
                @Override
                public void pushSuc(String type, String s1, int code, String s2, String response) throws RemoteException {
                    listener.pushSucceed(type, s1, code, s2, response);
                }

                @Override
                public void pushFail(String s, String s1, int i, String s2) throws RemoteException {
                    listener.pushFail(s, s1, i, s2);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
            listener.pushFail("", "", 0, "");
        }
    }

    public static void getProfile(Context context, String data, final PushResultListener listener) {
        getInfoWithKeyAndData(context, "profile", data, listener);
    }

    private static void getInfoWithKeyAndData(Context context, String key, String data, final PushResultListener listener) {
        getManager(context).getInfoWithKeyAndData(key, data, new IReadboyWearListener.Stub() {
            @Override
            public void pushSuc(String type, String s1, int code, String s2, String response) throws RemoteException {
                listener.pushSucceed(type, s1, code, s2, response);
            }

            @Override
            public void pushFail(String s, String s1, int i, String s2) throws RemoteException {
                listener.pushFail(s, s1, i, s2);
            }
        });
    }

    /**
     * 判断wifi是否连接状态
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     *
     * @param context 上下文
     * @return {@code true}: 连接<br>{@code false}: 未连接
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm == null ? null : cm.getActiveNetworkInfo();
        return info != null && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    private static String getHeadersString(Header[] headers) {
        if (headers == null || headers.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Header header : headers) {
            builder.append(header.getName()).append(":").append(header.getValue());
            builder.append("\n");
        }
        return builder.toString();
    }

}
