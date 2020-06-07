package com.bytearch.designPattern.singleton;

/**
 * 枚举单例
 */
public class Singleton {
    private Singleton() {
    }

    /**
     * 静态枚举
     */
     enum SingletonEnum {

        INSTANCE;

        private Singleton singleton;

        SingletonEnum() {
            singleton = new Singleton();
        }

        private Singleton getInstance() {
            return singleton;
        }
    }

    public static Singleton getInstance() {
        return SingletonEnum.INSTANCE.getInstance();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(()-> {
                System.out.println(Singleton.getInstance().hashCode());
            }).start();
        }
    }
}
