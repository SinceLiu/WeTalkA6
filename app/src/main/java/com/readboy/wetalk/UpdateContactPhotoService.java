package com.readboy.wetalk;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.readboy.wetalk.bean.Friend;

public class UpdateContactPhotoService extends IntentService {
    private static final String TAG = "hwj_UpdateContactSer";

    private static final String ICON_URL = "http://img.readboy.com/avatar/";
    private static final String RB_UPDATE_PHOTO_PER_HOUR = "RB_UPDATE_PHOTO_PER_HOUR";
    private static final int IMAGE_WIDTH = 126;

    public UpdateContactPhotoService() {
        super("UpdateContactPhotoService");
        // TODO Auto-generated constructor stub
    }

    static RequestQueue mQueue;

    public static RequestQueue getQueue(Context context) {
        if (mQueue == null) {
            synchronized (UpdateContactPhotoService.class) {
                if (mQueue == null) {
                    mQueue = Volley.newRequestQueue(context);
                }
            }
        }
        return mQueue;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        updateAllPhoto(getApplicationContext());
    }

    private ErrorListener errorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
            Log.i(TAG, "VolleyError = " + error.toString());
            //更新失败，置零，下次进入可以更新。
            if (error.networkResponse == null || error.networkResponse.statusCode != 404) {
                if (error.networkResponse != null) {
                    byte[] data = error.networkResponse.data;
                    String response = data != null ? new String(data) : "";
                    Log.i(TAG, "onErrorResponse: status = " + error.networkResponse.statusCode
                            + ", " + response);
                }
                handleError();
            }
        }
    };

    private void handleError() {
        Log.i(TAG, "handleError: ");
        try {
            Settings.Global.putLong(UpdateContactPhotoService.this.getContentResolver()
                    , RB_UPDATE_PHOTO_PER_HOUR, 0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "handleError: e = " + e.toString());
        }
    }

    public void updateAllPhoto(final Context context) {
//		getQueue(context).getCache().clear();
        Log.e(TAG, "updateAllPhoto() called with:");
        int oldId = -1;
        Friend contact = null;
        List<Friend> list = new ArrayList<Friend>();
        Cursor c = context.getContentResolver().query(Data.CONTENT_URI,
                new String[]{Data.RAW_CONTACT_ID, Data.MIMETYPE, "data8", Photo.PHOTO_URI},
                Data.MIMETYPE + "=? OR " + Data.MIMETYPE + "=?",
                new String[]{StructuredPostal.CONTENT_ITEM_TYPE, Photo.CONTENT_ITEM_TYPE},
                Data.RAW_CONTACT_ID);
        if (c != null && c.moveToFirst()) {
            do {
                int rawId = c.getInt(c.getColumnIndex(Data.RAW_CONTACT_ID));
                if (rawId != oldId) {
                    oldId = rawId;
                    contact = new Friend();
                    contact.contactId = rawId;
                    list.add(contact);
                }
                if (contact == null) {
                    continue;
                }
                String mimetype = c.getString(c.getColumnIndex(Data.MIMETYPE));
                switch (mimetype) {
                    case StructuredPostal.CONTENT_ITEM_TYPE:
                        contact.uuid = c.getString(c.getColumnIndex("data8"));
                        break;
                    case Photo.CONTENT_ITEM_TYPE:
                        contact.photoUri = c.getString(c.getColumnIndex(Photo.PHOTO_URI));
                        break;
                    default:
                        Log.e(TAG, "updateAllPhoto: default mimeType = " + mimetype);
                }
            } while (c.moveToNext());
            c.close();
        }
        for (Friend rbContact : list) {
            final long rawContactId = rbContact.contactId;
            if (TextUtils.isEmpty(rbContact.uuid) || rbContact.uuid.startsWith("G")) {
                //过滤家庭圈。
                continue;
            }
            String url = ICON_URL + rbContact.uuid;
            if (TextUtils.isEmpty(rbContact.photoUri)) {
                ImageRequest request = new ImageRequest(url, new Listener<Bitmap>() {

                    @Override
                    public void onResponse(Bitmap response) {
                        // TODO Auto-generated method stub
                        insertPhoto(context, rawContactId, response);
                    }
                }, IMAGE_WIDTH, IMAGE_WIDTH, ImageView.ScaleType.CENTER_INSIDE, Config.ARGB_8888, errorListener);
                getQueue(context).add(request);
            } else {
                ImageRequest request = new ImageRequest(url, new Listener<Bitmap>() {

                    @Override
                    public void onResponse(Bitmap response) {
                        // TODO Auto-generated method stub
                        updateContactPhoto(context, rawContactId, response);
                    }
                }, IMAGE_WIDTH, IMAGE_WIDTH, ImageView.ScaleType.CENTER_INSIDE, Config.ARGB_8888, errorListener);
                getQueue(context).add(request);
            }
        }
    }

    //	private static void saveBitmap(Bitmap bmp,String name){
//		if (Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED)) {
//            String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test/";
//            Log.i("adsd",dir);
//            File fileDir = new File(dir);
//            if (!fileDir.exists()) {
//                fileDir.mkdirs();
//            }
//            File file = new File(dir, name + ".png");
//            try {
//                FileOutputStream fos = new FileOutputStream(file);
//                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
//
//                fos.flush();
//                fos.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//	}

    /**
     * 插入一个联系人的头像
     */
    private void insertPhoto(Context context, long rawContactId, Bitmap bmp) {
        if (bmp == null) {
            return;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
        byte[] avatar = os.toByteArray();
        ContentValues cv = new ContentValues();
        cv.put(Data.RAW_CONTACT_ID, rawContactId);
        cv.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
        cv.put(Photo.PHOTO, avatar);
        Uri uri = context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
        Log.i(TAG, "insertPhoto: uri = " + uri);
        if (uri == null) {
            handleError();
        }
    }

    /**
     * 更新头像
     */
    private void updateContactPhoto(Context context, long rawContactId, Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        ContentValues values = new ContentValues();
        values.put(Photo.PHOTO, data);
        int rows = context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
                ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=? ",
                new String[]{rawContactId + "", ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE});
        if (rows <= 0) {
            handleError();
        }
    }
}
