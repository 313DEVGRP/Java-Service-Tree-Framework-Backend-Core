package com.arms.egovframework.javaservice.treeframework.util;

import java.util.UUID;

public class UUIDTimeProvider {

    private UUIDTimeProvider(){

    }
    public static String  getCurrentTimeUUID() {
        return UUID.randomUUID().toString();
    }
}
