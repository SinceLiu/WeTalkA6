package com.readboy.wetalk;

import com.readboy.bean.Model;
import com.readboy.utils.EmojiUtils;

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
        EmojiUtils.test();
        String s = EmojiUtils.test2();
        String s1 = s;

    }
}