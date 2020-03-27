package com.guangzhida.xiaomai.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Util {
    /**
     * 编码
     *
     * @param message 需编码的信息
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encodeWord(String message) throws UnsupportedEncodingException {

        return Base64.encodeToString(message.getBytes("utf-8"), Base64.NO_WRAP);

    }

    /**
     * 解码
     *
     * @param encodeWord 编码后的内容
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String decodeWord(String encodeWord) throws UnsupportedEncodingException {

        return new String(Base64.decode(encodeWord, Base64.NO_WRAP), "utf-8");

    }
}
