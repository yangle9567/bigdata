package com.bcld.utils;

import java.util.UUID;

public class IdUtils {
    public static String generateUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
