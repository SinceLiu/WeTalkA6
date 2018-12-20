package com.readboy.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 数据库常量
 *
 * @author oubin
 * @date 2018/1/30
 */

public class WeTalkContract {

    public static final String AUTHORITY = "com.readboy.wetalk.provider.Conversation";

    /**
     * uuid : DA59F29B4E001B63
     * imei : 868706020000215
     * name : 宝贝
     * type : W5
     * sex : 0
     * grade : 0
     */
    public interface ProfileColumns extends BaseColumns {

        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/profiles");

        String PROFILE_TABLE_NAME = "profile";


        /**
         * UUID，和imei一一对应关系
         */
        String UUID = "uuid";
        String IMEI = "imei";
        String NAME = "name";
        String TYPE = "type";
        /**
         * 性别
         * 0代表男性，1代表女性
         * value数据类型:int
         */
        String SEX = "sex";
        /**
         * hwj_UpdateContactSer
         * 年级信息
         * value: 数据类型:int
         * value: 0代表一年级
         */
        String GRADE = "grade";
        /**
         * json结构String
         */
        String DATA = "data";

        int INDEX_UUID = 1;
        int INDEX_IMEI = 2;
        int INDEX_NAME = 3;
        int INDEX_TYPE = 4;
        int INDEX_SEX = 5;
        int INDEX_GRADE = 6;
        int INDEX_DATA = 7;

    }

}
