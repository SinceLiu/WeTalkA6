package com.readboy.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import com.readboy.wetalk.R;

/**
 * Created by hwwjian on 2016/12/2.
 */

public class EmojiUtils {
    
    private static final Map<String,Integer> EMOJI_CODE = new LinkedHashMap<>();
    
    static{
    	EMOJI_CODE.put("/emoji_1",R.drawable.emoji1);
        EMOJI_CODE.put("/emoji_2",R.drawable.emoji2);
        EMOJI_CODE.put("/emoji_3",R.drawable.emoji3);
        EMOJI_CODE.put("/emoji_4",R.drawable.emoji4);
        EMOJI_CODE.put("/emoji_17",R.drawable.emoji17);
        EMOJI_CODE.put("/emoji_6",R.drawable.emoji6);
        EMOJI_CODE.put("/emoji_7",R.drawable.emoji7);
        EMOJI_CODE.put("/emoji_8",R.drawable.emoji8);
        EMOJI_CODE.put("/emoji_9",R.drawable.emoji9);
        EMOJI_CODE.put("/emoji_10",R.drawable.emoji10);
        EMOJI_CODE.put("/emoji_11",R.drawable.emoji11);
        EMOJI_CODE.put("/emoji_12",R.drawable.emoji12);
        EMOJI_CODE.put("/emoji_13",R.drawable.emoji13);
        EMOJI_CODE.put("/emoji_14",R.drawable.emoji14);
        EMOJI_CODE.put("/emoji_15",R.drawable.emoji15);
        EMOJI_CODE.put("/emoji_16",R.drawable.emoji16);
        EMOJI_CODE.put("/emoji_5",R.drawable.emoji5);
        EMOJI_CODE.put("/emoji_18",R.drawable.emoji18);
        EMOJI_CODE.put("/emoji_19",R.drawable.emoji19);
        EMOJI_CODE.put("/emoji_20",R.drawable.emoji20);
        EMOJI_CODE.put("/emoji_21",R.drawable.emoji21);
        EMOJI_CODE.put("/emoji_22",R.drawable.emoji22);
        EMOJI_CODE.put("/emoji_23",R.drawable.emoji23);
        EMOJI_CODE.put("/emoji_24",R.drawable.emoji24);
        EMOJI_CODE.put("/emoji_25",R.drawable.emoji25);
        EMOJI_CODE.put("/emoji_26",R.drawable.emoji26);
        EMOJI_CODE.put("/emoji_27",R.drawable.emoji27);
        EMOJI_CODE.put("/emoji_28",R.drawable.emoji28);
        EMOJI_CODE.put("/emoji_29",R.drawable.emoji29);
        EMOJI_CODE.put("/emoji_30",R.drawable.emoji30);
        EMOJI_CODE.put("/emoji_31",R.drawable.emoji31);
        EMOJI_CODE.put("/emoji_32",R.drawable.emoji32);
        EMOJI_CODE.put("/emoji_33",R.drawable.emoji33);
        EMOJI_CODE.put("/emoji_34",R.drawable.emoji34);
        EMOJI_CODE.put("/emoji_35",R.drawable.emoji35);
        EMOJI_CODE.put("/emoji_36",R.drawable.emoji36);
        EMOJI_CODE.put("/emoji_37",R.drawable.emoji37);
        EMOJI_CODE.put("/emoji_38",R.drawable.emoji38);
        EMOJI_CODE.put("/emoji_39",R.drawable.emoji39);
        EMOJI_CODE.put("/emoji_40",R.drawable.emoji40);
        EMOJI_CODE.put("/emoji_41",R.drawable.emoji41);
        EMOJI_CODE.put("/emoji_42",R.drawable.emoji42);
        EMOJI_CODE.put("/emoji_43",R.drawable.emoji43);
        EMOJI_CODE.put("/emoji_44",R.drawable.emoji44);
        EMOJI_CODE.put("/emoji_45",R.drawable.emoji45);
        EMOJI_CODE.put("/emoji_46",R.drawable.emoji46);
        EMOJI_CODE.put("/emoji_47",R.drawable.emoji47);
        EMOJI_CODE.put("/emoji_48",R.drawable.emoji48);
        EMOJI_CODE.put("/emoji_49",R.drawable.emoji49);
        EMOJI_CODE.put("/emoji_50",R.drawable.emoji50);
        EMOJI_CODE.put("/emoji_51",R.drawable.emoji51);
    }
    
    public static final int[] ALL_EMOJI_SINGLE_ID = {
    	R.drawable.emoji1,R.drawable.emoji2,R.drawable.emoji3,R.drawable.emoji4,R.drawable.emoji17, R.drawable.emoji6,
        R.drawable.emoji7,R.drawable.emoji8,R.drawable.emoji9,R.drawable.emoji50,
        R.drawable.emoji11,R.drawable.emoji12,R.drawable.emoji13,R.drawable.emoji14,R.drawable.emoji15, R.drawable.emoji16,
        R.drawable.emoji34,R.drawable.emoji18,R.drawable.emoji19,R.drawable.emoji20,
        R.drawable.emoji21,R.drawable.emoji22,R.drawable.emoji23,R.drawable.emoji24,R.drawable.emoji25, R.drawable.emoji26,
        R.drawable.emoji27,R.drawable.emoji28,R.drawable.emoji29,R.drawable.emoji30,
        R.drawable.emoji31,R.drawable.emoji32,R.drawable.emoji33,R.drawable.emoji5,R.drawable.emoji35, R.drawable.emoji36,
        R.drawable.emoji37,R.drawable.emoji38,R.drawable.emoji39,R.drawable.emoji40,
        R.drawable.emoji41,R.drawable.emoji42,R.drawable.emoji43,R.drawable.emoji44,R.drawable.emoji45, R.drawable.emoji46,
        R.drawable.emoji47,R.drawable.emoji48,R.drawable.emoji49,R.drawable.emoji10,
        R.drawable.emoji51
    };

    /**
     * 根据编码获取表情的本地Id
     * @param code 编码
     * @return 本地Id
     */
    public static int getEmojiId(String code){
       Integer id = EMOJI_CODE.get(code);
        if(id != null){
            return id;
        }else{
            return -1;
        }
    }

    /**
     * 根据本地Id
     * @param id
     * @return
     */
    public static String getEmojiCode(int id){
        for(String key : EMOJI_CODE.keySet()){
            if(id == EMOJI_CODE.get(key)){
                return key;
            }
        }
        return "";
    }
    
}
