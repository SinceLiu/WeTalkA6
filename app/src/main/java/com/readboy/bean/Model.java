package com.readboy.bean;

import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;

/**
 * @author oubin
 * @date 2017/12/20
 * W2S：0x20
 * W2T：0x21
 * W3T：0x10
 * W5：0x30
 * A3：0x40
 * W7：0x41
 * Imei前缀：
 * W2S：86031403
 * W2T：86742703
 * W3T：86989302
 * W5：86870602
 * A3：86694403
 * W7：86578203
 */

public enum Model implements Serializable {

    W2S(0x20, "8603140"),
    W2T(0x21, "8674270"),
    W3T(0x10, "8698930"),
    W5(0x30, "8687060"),
    A3(0x40, "8669440"),
    W7(0x41, "8657820");

    private static final String TAG = "hwj_Model";
    private static final String READBOY_BLUETOOTH_ADDRESS_PREFIX = "07:C1";
    /**
     * uuid开头为D的，代表是手表用户
     */
    public static final String UUID_BEGINNING_CHARACTER = "D";
    public static final int DEFAULT_MAX_RECORD_TIME = 15;
    public static final int MAX_RECORD_TIME_SHORT = 10;

    /**
     * 蓝牙地址UAP, 蓝牙地址第17-24位。用于标识设备型号。
     */
    private int uap;
    /**
     * 不同型号，不同imei前缀，7为数字。
     */
    private String imeiPrefix;

    private boolean canSendPicture;
    private boolean canSendExpression;

    Model(int uap, String imeiPrefix) {
        this.uap = uap;
        this.imeiPrefix = imeiPrefix;
    }

    public int getUap() {
        return uap;
    }

    public String getImeiPrefix() {
        return imeiPrefix;
    }

    public static Model getModel(int uap) {
        for (Model model : Model.values()) {
            if (model.uap == uap) {
                return model;
            }
        }
        return null;
    }

    /**
     * 获取型号
     *
     * @param imei 完整imei
     * @return 通过imei前缀判断机型，如果不是
     */
    public static Model getModel(String imei) {
        for (Model model : Model.values()) {
            if (imei.startsWith(model.getImeiPrefix())) {
                return model;
            }
        }
        return null;
    }

    public static boolean isAndroidSystemDevice(String imei) {
        Model model = null;
        for (Model m : Model.values()) {
            if (imei.startsWith(m.getImeiPrefix())) {
                model = m;
            }
        }
        return model == null || model.getUap() >= 0x40;
    }

    /**
     * 根据蓝牙地址推算IMEI
     *
     * @param bluetoothAddress 蓝牙地址, 00:A0:C6:CA:D8:FA
     * @return 完整imei
     */
    public static String computeImei(String bluetoothAddress) {
//        Log.e(TAG, "computeImei() called with: bluetoothAddress = " + bluetoothAddress + "");
        StringBuilder result = new StringBuilder(15);
        String[] tempArray = bluetoothAddress.split(":");
        if (tempArray.length == 6) {
            int uap = Integer.valueOf(tempArray[2], 16);
            Model model = Model.getModel(uap);
            if (model == null) {
                return "";
            }
            String prefix = tempArray[3] + tempArray[4] + tempArray[5];
            int imeiCell = Integer.valueOf(prefix, 16);
            if (imeiCell > 0x9E8B00) {
                imeiCell -= 100;
            }
            result.append(model.getImeiPrefix()).append(imeiCell);
//            Log.e(TAG, "computeImei: result 14 = " + result.toString());
            result.append(genCode(result.toString()));
//            Log.e(TAG, "computeImei: result 15 = " + result.toString());
        }

        return result.toString();
    }

    /**
     * (1).将偶数位数字分别乘以2,分别计算个位数和十位数之和
     * (2).将奇数位数字相加,再加上上一步算得的值
     * (3).如果得出的数个位是0则校验位为0,否则为10减去个位数
     * 如:86 57 82 03 00 00 30
     * 偶数位乘以2得到 6*2=12 7*2=14 2*2=04 3*2=06 0*2=00 0*2=00 0*2=00,
     * 计算奇数位数字之和和偶数位个位十位之和,
     * 得到 8+(1+2)+5+(1+4)+8+(0+4)+0+(0+6)+0+(0+0)+0+(0+0)+3+(0+0)=42 => 校验位 10-2 = 8
     * 86 57 82 03 00 00 30 8
     *
     * @param code
     * @return
     */
    private static String genCode(String code) {
        if (code.length() == 14) {
            int total = 0, sum1 = 0, sum2 = 0;
            int temp = 0;
            char[] chs = code.toCharArray();
            for (int i = 0; i < chs.length; i++) {
                int num = chs[i] - '0';
                //System.out.println(num);
                /*(1)将奇数位数字相加(从1开始计数)*/
                if (i % 2 == 0) {
                    sum1 = sum1 + num;
                } else {
                    /*(2)将偶数位数字分别乘以2,分别计算个位数和十位数之和(从1开始计数)*/
                    temp = num * 2;
                    if (temp < 10) {
                        sum2 += temp;
                    } else {
                        sum2 += temp + 1 - 10;
                    }
                }
            }
            total = sum1 + sum2;
            /*如果得出的数个位是0则校验位为0,否则为10减去个位数 */
            if (total % 10 == 0) {
                return "0";
            } else {
                return (10 - (total % 10)) + "";
            }
        } else {
            return "";
        }
    }

    /**
     * 不同设备限制的录音时长不一样，单位:毫秒
     */
    public static int getMaxRecordTime(Model model){
        switch (model) {
            case W2S:
            case W2T:
            case W3T:
            case W5:
                return MAX_RECORD_TIME_SHORT;
        }
        return DEFAULT_MAX_RECORD_TIME;
    }

    @Override
    public String toString() {
        return "Model{" +
                "uap=" + uap +
                ", imeiPrefix='" + imeiPrefix + '\'' +
                '}';
    }
}
