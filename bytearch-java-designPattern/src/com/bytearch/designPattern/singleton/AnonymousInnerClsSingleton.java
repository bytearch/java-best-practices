package com.bytearch.designPattern.singleton;

/**
 * 匿名内部类
 * 加载外部类时不会加载内部类
 */
public class AnonymousInnerClsSingleton {

    private AnonymousInnerClsSingleton() {
    }

    private static class LazyHolder {
        private final static AnonymousInnerClsSingleton INSTANCE = new AnonymousInnerClsSingleton();
    }

    public static AnonymousInnerClsSingleton getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(()-> {
                System.out.println(AnonymousInnerClsSingleton.getInstance().hashCode());
            }).start();
        }
    }
}
