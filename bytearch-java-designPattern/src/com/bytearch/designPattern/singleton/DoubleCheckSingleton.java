package com.bytearch.designPattern.singleton;

/**
 * 懒汉双重验证
 */
public class DoubleCheckSingleton {
    private  volatile static DoubleCheckSingleton INSTANCE = null;

    private DoubleCheckSingleton() {
    }

    public static DoubleCheckSingleton getInstance() {
        if (INSTANCE == null) {
            synchronized (DoubleCheckSingleton.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DoubleCheckSingleton();
                }
            }
        }
        return INSTANCE;
    }
}
