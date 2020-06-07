package com.bytearch.designPattern.singleton;

/**
 * 饿汉式
 */
public class EaterSingleton {
    private static final EaterSingleton INSTANCE = new EaterSingleton();

    private EaterSingleton() {
    }

    public static EaterSingleton getInstance() {
        return INSTANCE;
    }
}
