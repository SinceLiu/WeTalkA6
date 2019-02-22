package com.readboy.wetalk;



import android.app.readboy.ReadboyWearManager;
import android.support.v4.app.Fragment;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void bluetoothTest() throws Exception {
        String code = "/yiw";
//        int i = EmojiUtils.getEmojiIdContainOldCode(code);
//        EmojiUtils.test();
//        String s = EmojiUtils.test2();
//        String s1 = s;
//
//        int i = 1<<7;
//        String hex = Integer.toBinaryString(i);
//        hex = Integer.toHexString(i);

        String str1 = "zhong中";
        String str2 = "zhogn";
        String str3 = "中国";
        byte[] data = new byte[10];
        String str4 = new String(data);
        String str5 = new String(str1.getBytes());
        int length1 = str1.length();
        int length2 = str2.length();
        int length3 = str3.length();

        String lcm = "0-1h154q01_vdo";
        int index = lcm.indexOf("-");
        String result = lcm.substring(index + 1, lcm.length());
        boolean b = "1h154q01_vdo".equals(result);
        if (b) {

        }

    }


}