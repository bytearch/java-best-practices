package com.bytearch.designPattern.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器式单例模式
 */
public class ContainerSingleton {
    private ContainerSingleton() {
    }
    private static Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    public static Object getBean(String className) {
        Object singletonObject = singletonObjects.get(className);
        if (singletonObject == null) {
            synchronized (singletonObjects) {
                singletonObject =  singletonObjects.get(className);
                if (singletonObject == null) {
                    try {
                        try {
                            singletonObject = Class.forName(className).newInstance();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        singletonObjects.put(className, singletonObject);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return singletonObject;
    }

}
