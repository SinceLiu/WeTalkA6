package com.readboy.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.readboy.wetalk.R;

/**
 * @author hwwjian
 * @date 2016/12/2
 */

public class EmojiUtils {
    private static final String TAG = "EmojiUtils";

    private static final Map<String, Integer> EMOJI_CODE = new LinkedHashMap<>();

    /**
     * key: 新表情编码
     * value: 旧表情编码
     */
    private static final Map<String, String> CODING_TABLE = new HashMap<>();

    static {
        EMOJI_CODE.put("/emoji_1", R.drawable.emoji1);
        EMOJI_CODE.put("/emoji_2", R.drawable.emoji2);
        EMOJI_CODE.put("/emoji_3", R.drawable.emoji3);
        EMOJI_CODE.put("/emoji_4", R.drawable.emoji4);
        EMOJI_CODE.put("/emoji_5", R.drawable.emoji5);
        EMOJI_CODE.put("/emoji_6", R.drawable.emoji6);
        EMOJI_CODE.put("/emoji_7", R.drawable.emoji7);
        EMOJI_CODE.put("/emoji_8", R.drawable.emoji8);
        EMOJI_CODE.put("/emoji_9", R.drawable.emoji9);
        EMOJI_CODE.put("/emoji_10", R.drawable.emoji10);
        EMOJI_CODE.put("/emoji_11", R.drawable.emoji11);
        EMOJI_CODE.put("/emoji_12", R.drawable.emoji12);
        EMOJI_CODE.put("/emoji_13", R.drawable.emoji13);
        EMOJI_CODE.put("/emoji_14", R.drawable.emoji14);
        EMOJI_CODE.put("/emoji_15", R.drawable.emoji15);
        EMOJI_CODE.put("/emoji_16", R.drawable.emoji16);
        EMOJI_CODE.put("/emoji_17", R.drawable.emoji17);
        EMOJI_CODE.put("/emoji_18", R.drawable.emoji18);
        EMOJI_CODE.put("/emoji_19", R.drawable.emoji19);
        EMOJI_CODE.put("/emoji_20", R.drawable.emoji20);
        EMOJI_CODE.put("/emoji_21", R.drawable.emoji21);
        EMOJI_CODE.put("/emoji_22", R.drawable.emoji22);
        EMOJI_CODE.put("/emoji_23", R.drawable.emoji23);
        EMOJI_CODE.put("/emoji_24", R.drawable.emoji24);
        EMOJI_CODE.put("/emoji_25", R.drawable.emoji25);
        EMOJI_CODE.put("/emoji_26", R.drawable.emoji26);
        EMOJI_CODE.put("/emoji_27", R.drawable.emoji27);
        EMOJI_CODE.put("/emoji_28", R.drawable.emoji28);
        EMOJI_CODE.put("/emoji_29", R.drawable.emoji29);
        EMOJI_CODE.put("/emoji_30", R.drawable.emoji30);
        EMOJI_CODE.put("/emoji_31", R.drawable.emoji31);
        EMOJI_CODE.put("/emoji_32", R.drawable.emoji32);
        EMOJI_CODE.put("/emoji_33", R.drawable.emoji33);
        EMOJI_CODE.put("/emoji_34", R.drawable.emoji34);
        EMOJI_CODE.put("/emoji_35", R.drawable.emoji35);
        EMOJI_CODE.put("/emoji_36", R.drawable.emoji36);
        EMOJI_CODE.put("/emoji_37", R.drawable.emoji37);
        EMOJI_CODE.put("/emoji_38", R.drawable.emoji38);
        EMOJI_CODE.put("/emoji_39", R.drawable.emoji39);
        EMOJI_CODE.put("/emoji_40", R.drawable.emoji40);
        EMOJI_CODE.put("/emoji_41", R.drawable.emoji41);
        EMOJI_CODE.put("/emoji_42", R.drawable.emoji42);
        EMOJI_CODE.put("/emoji_43", R.drawable.emoji43);
        EMOJI_CODE.put("/emoji_44", R.drawable.emoji44);
        EMOJI_CODE.put("/emoji_45", R.drawable.emoji45);
        EMOJI_CODE.put("/emoji_46", R.drawable.emoji46);
        EMOJI_CODE.put("/emoji_47", R.drawable.emoji47);
        EMOJI_CODE.put("/emoji_48", R.drawable.emoji48);
        EMOJI_CODE.put("/emoji_49", R.drawable.emoji49);
        EMOJI_CODE.put("/emoji_50", R.drawable.emoji50);
        EMOJI_CODE.put("/emoji_51", R.drawable.emoji51);

        EMOJI_CODE.put("/ch", R.drawable.smiley_1);
        EMOJI_CODE.put("/zj", R.drawable.smiley_2);
        EMOJI_CODE.put("/shuai", R.drawable.smiley_5);
        EMOJI_CODE.put("/xu", R.drawable.smiley_12);
        EMOJI_CODE.put("/jie", R.drawable.smiley_17);
        EMOJI_CODE.put("/tu", R.drawable.smiley_22);
        EMOJI_CODE.put("/zk", R.drawable.smiley_23);
        EMOJI_CODE.put("/tp", R.drawable.smiley_29);
        EMOJI_CODE.put("/:)", R.drawable.smiley_41);
        EMOJI_CODE.put("/ty", R.drawable.smiley_42);
        EMOJI_CODE.put("/yl", R.drawable.smiley_43);
        EMOJI_CODE.put("/bb", R.drawable.smiley_44);
        EMOJI_CODE.put("/piaoch", R.drawable.smiley_45);
        EMOJI_CODE.put("/zq", R.drawable.smiley_46);
        EMOJI_CODE.put("/dao", R.drawable.smiley_47);
        EMOJI_CODE.put("/shd", R.drawable.smiley_49);
        EMOJI_CODE.put("/dg", R.drawable.smiley_50);
        EMOJI_CODE.put("/xs", R.drawable.smiley_51);
        EMOJI_CODE.put("/xin", R.drawable.smiley_52);
        EMOJI_CODE.put("/dx", R.drawable.smiley_54);
        EMOJI_CODE.put("/mg", R.drawable.smiley_55);
        EMOJI_CODE.put("/xig", R.drawable.smiley_56);
        EMOJI_CODE.put("/pj", R.drawable.smiley_57);
        EMOJI_CODE.put("/lq", R.drawable.smiley_58);
        EMOJI_CODE.put("/pp", R.drawable.smiley_59);
        EMOJI_CODE.put("/kf", R.drawable.smiley_60);
        EMOJI_CODE.put("/zt", R.drawable.smiley_62);
        EMOJI_CODE.put("/qq", R.drawable.smiley_66);
        EMOJI_CODE.put("/cd", R.drawable.smiley_69);
        EMOJI_CODE.put("/hq", R.drawable.smiley_71);
        EMOJI_CODE.put("/zhh", R.drawable.smiley_73);
        EMOJI_CODE.put("/gz", R.drawable.smiley_76);
        EMOJI_CODE.put("/hd", R.drawable.smiley_78);
        EMOJI_CODE.put("/shl", R.drawable.smiley_79);
        EMOJI_CODE.put("/bu", R.drawable.smiley_80);
        EMOJI_CODE.put("/ws", R.drawable.smiley_81);
        EMOJI_CODE.put("/aini", R.drawable.smiley_82);
        EMOJI_CODE.put("/ruo", R.drawable.smiley_83);
        EMOJI_CODE.put("/cj", R.drawable.smiley_84);
        EMOJI_CODE.put("/qiang", R.drawable.smiley_85);
        EMOJI_CODE.put("/qt", R.drawable.smiley_86);
        EMOJI_CODE.put("/yb", R.drawable.smiley_87);
        EMOJI_CODE.put("/gy", R.drawable.smiley_88);
        EMOJI_CODE.put("/lw", R.drawable.smiley_89);
    }

    static {
        CODING_TABLE.put("/emoji_1", "/kel");
        CODING_TABLE.put("/emoji_2", "/zhm");
        CODING_TABLE.put("/emoji_3", "/emoji3");
        CODING_TABLE.put("/emoji_4", "/kb");
        CODING_TABLE.put("/emoji_5", "/jk");
        CODING_TABLE.put("/emoji_6", "/db");
        CODING_TABLE.put("/emoji_7", "/cy");
        CODING_TABLE.put("/emoji_8", "/zhem");
        CODING_TABLE.put("/emoji_9", "/wq");
        CODING_TABLE.put("/emoji_10", "/kk");
        CODING_TABLE.put("/emoji_11", "/hanx");
        CODING_TABLE.put("/emoji_12", "/gg");
        CODING_TABLE.put("/emoji_13", "/fan");
        CODING_TABLE.put("/emoji_14", "/yun");
        CODING_TABLE.put("/emoji_15", "/yiw");
        CODING_TABLE.put("/emoji_16", "/emoji16");
        CODING_TABLE.put("/emoji_17", "/fn");
        CODING_TABLE.put("/emoji_18", "/kun");
        CODING_TABLE.put("/emoji_19", "/emoji19");
        CODING_TABLE.put("/emoji_20", "/wen");
        CODING_TABLE.put("/emoji_21", "/lengh");
        CODING_TABLE.put("/emoji_22", "/dk");
        CODING_TABLE.put("/emoji_23", "/am");
        CODING_TABLE.put("/emoji_24", "/pz");
        CODING_TABLE.put("/emoji_25", "/baiy");
        CODING_TABLE.put("/emoji_26", "/qiao");
        CODING_TABLE.put("/emoji_27", "/bz");
        CODING_TABLE.put("/emoji_28", "/zhd");
        CODING_TABLE.put("/emoji_29", "/se");
        CODING_TABLE.put("/emoji_30", "/yx");
        CODING_TABLE.put("/emoji_31", "/bq");
        CODING_TABLE.put("/emoji_32", "/ch");
        CODING_TABLE.put("/emoji_33", "/kuk");
        CODING_TABLE.put("/emoji_34", "/kl");
        CODING_TABLE.put("/emoji_35", "/fendou");
        CODING_TABLE.put("/emoji_36", "/ll");
        CODING_TABLE.put("/emoji_37", "/wq");
        CODING_TABLE.put("/emoji_38", "/dy");
        CODING_TABLE.put("/emoji_39", "/hx");
        CODING_TABLE.put("/emoji_40", "/shui");
        CODING_TABLE.put("/emoji_41", "/huaix");
        CODING_TABLE.put("/emoji_42", "/tx");
        CODING_TABLE.put("/emoji_43", "/jy");
        CODING_TABLE.put("/emoji_44", "/fd");
        CODING_TABLE.put("/emoji_45", "/xia");
        CODING_TABLE.put("/emoji_46", "/qd");
        CODING_TABLE.put("/emoji_47", "/yhh");
        CODING_TABLE.put("/emoji_48", "/ka");
        CODING_TABLE.put("/emoji_49", "/ng");
        CODING_TABLE.put("/emoji_50", "/lh");
        CODING_TABLE.put("/emoji_51", "/jk");
    }

    public static final int[] ALL_EMOJI_SINGLE_ID = {
            R.drawable.emoji1, R.drawable.emoji2, R.drawable.emoji3, R.drawable.emoji4, R.drawable.emoji17,
            R.drawable.emoji6, R.drawable.emoji7, R.drawable.emoji8, R.drawable.emoji9, R.drawable.emoji50,
            R.drawable.emoji11, R.drawable.emoji12, R.drawable.emoji13, R.drawable.emoji14, R.drawable.emoji15,
            R.drawable.emoji16, R.drawable.emoji34, R.drawable.emoji18, R.drawable.emoji19, R.drawable.emoji20,
            R.drawable.emoji21, R.drawable.emoji22, R.drawable.emoji23, R.drawable.emoji24, R.drawable.emoji25,
            R.drawable.emoji26, R.drawable.emoji27, R.drawable.emoji28, R.drawable.emoji29, R.drawable.emoji30,
            R.drawable.emoji31, R.drawable.emoji32, R.drawable.emoji33, R.drawable.emoji5, R.drawable.emoji35,
            R.drawable.emoji36, R.drawable.emoji37, R.drawable.emoji38, R.drawable.emoji39, R.drawable.emoji40,
            R.drawable.emoji41, R.drawable.emoji42, R.drawable.emoji43, R.drawable.emoji44, R.drawable.emoji45,
            R.drawable.emoji46, R.drawable.emoji47, R.drawable.emoji48, R.drawable.emoji49, R.drawable.emoji10,
            R.drawable.emoji51
    };

    /**
     * 根据编码获取表情的本地Id
     *
     * @param code 编码
     * @return 本地Id
     */
    public static int getEmojiId(String code) {
        Integer id = EMOJI_CODE.get(code);
        if (id != null) {
            return id;
        } else {
            return -1;
        }
    }

    /**
     * 根据本地Id
     *
     * @param id
     * @return
     */
    public static String getEmojiCode(int id) {
        for (String key : EMOJI_CODE.keySet()) {
            if (id == EMOJI_CODE.get(key)) {
                return key;
            }
        }
        return "";
    }

    /**
     * 先查找新表情中是否又对应的，没有就返回旧表情图片ID
     *
     * @param code 旧表情编码
     * @return -1代表找不到对应的表情编码。
     */
    public static int getEmojiIdContainOldCode(String code) {
//        LogInfo.e(TAG, "getEmojiIdContainOldCode() called with: code = " + code + "");
        if (TextUtils.isEmpty(code)) {
            return -1;
        }
        int result = -1;

        int id = getEmojiId(code);
        if (id != -1) {
            return id;
        }
        String newCode = null;
        for (String s : CODING_TABLE.keySet()) {
            if (code.equals(CODING_TABLE.get(s))) {
                newCode = s;
                break;
            }
        }
        if (!TextUtils.isEmpty(newCode)) {
            return getEmojiId(newCode);
        }
        return result;
    }

    /**
     * 针对W5，发送前转码
     */
    public static String getOldCode(int resId) {
        String newCode = getEmojiCode(resId);
        if (!TextUtils.isEmpty(newCode)) {
            return CODING_TABLE.get(newCode);
        }
        return "";
    }

    public static void test() {
        StringBuilder builder = new StringBuilder();
        for (String s : CODING_TABLE.keySet()) {
            for (String s1 : CODING_TABLE.keySet()) {
                if (s != s1 && CODING_TABLE.get(s).equals(CODING_TABLE.get(s1))) {
                    builder.append(s);
                    builder.append(" = ").append(s1);
                    builder.append(" \r\n ");
                }
            }
        }
        String result = builder.toString();
    }

    public static String test2() {
        StringBuilder builder = new StringBuilder();
        for (String s : CODING_TABLE.values()) {
            for (String s1 : EMOJI_CODE.keySet()) {
                if (s.equals(s1)) {
                    builder.append(s);
                    builder.append(" = ").append(s1);
                    builder.append(" \r\n ");
                }
            }
        }
        return builder.toString();
    }

    public static Collection<String> test3() {
        Collection<String> collection = new ArrayList<>();
        for (String s : EMOJI_CODE.keySet()) {
            if (!s.startsWith("/emoji")) {
                collection.add(s);
            }
        }
        return collection;
    }

}
