package com.iip.nju.util;

import java.util.UUID;

/**
 * Created by xu on 2017/1/5.
 */
public class UUIDutil {
    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
