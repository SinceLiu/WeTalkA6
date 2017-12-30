package com.readboy.wetalk;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.readboy.bean.Friend;

public class UpdateContactPhotoService extends IntentService {

    private static final String ICON_URL = "http://img.readboy.com/avatar/";
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

    private static ErrorListener errorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
            Log.i("adsd", "VolleyError = " + error.toString());
        }
    };

    public static void updateAllPhoto(final Context context) {
//		getQueue(context).getCache().clear();
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
                String mimetype = c.getString(c.getColumnIndex(Data.MIMETYPE));
                switch (mimetype) {
                    case StructuredPostal.CONTENT_ITEM_TYPE:
                        contact.uuid = c.getString(c.getColumnIndex("data8"));
                        break;
                    case Photo.CONTENT_ITEM_TYPE:
                        contact.photoUri = c.getString(c.getColumnIndex(Photo.PHOTO_URI));

                }
            } while (c.moveToNext());
            c.close();
        }
//		Log.i("adsd",list.toString());
        Iterator<Friend> iterator = list.iterator();
        while (iterator.hasNext()) {
            Friend rbContact = (Friend) iterator.next();
            final long rawContactId = rbContact.contactId;
            String url = ICON_URL + rbContact.uuid;
            if (TextUtils.isEmpty(rbContact.photoUri)) {
                ImageRequest request = new ImageRequest(url, new Listener<Bitmap>() {

                    @Override
                    public void onResponse(Bitmap response) {
                        // TODO Auto-generated method stub
                        Log.i("adsd", "insert onResponse success");
                        insertPhoto(context, rawContactId, response);
                    }
                }, IMAGE_WIDTH, IMAGE_WIDTH, ImageView.ScaleType.CENTER_INSIDE, Config.ARGB_8888, errorListener);
                getQueue(context).add(request);
            } else {
                ImageRequest request = new ImageRequest(url, new Listener<Bitmap>() {

                    @Override
                    public void onResponse(Bitmap response) {
                        // TODO Auto-generated method stub
                        Log.i("adsd", "update onResponse success");
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
    /*
     * 插入一个联系人的头像
	 */
    private static void insertPhoto(Context context, long rawContactId, Bitmap bmp) {
        if (bmp == null) return;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
        byte[] avatar = os.toByteArray();
        ContentValues cv = new ContentValues();
        cv.put(Data.RAW_CONTACT_ID, rawContactId);
        cv.put(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
        cv.put(Photo.PHOTO, avatar);
        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
    }

    /**
     * 更新头像
     *
     * @param context
     * @param rawContactId
     * @param bmp
     */
    public static void updateContactPhoto(Context context, long rawContactId, Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        ContentValues values = new ContentValues();
        values.put(Photo.PHOTO, data);
        context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values,
                ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=? ",
                new String[]{rawContactId + "", ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE});
    }
}
