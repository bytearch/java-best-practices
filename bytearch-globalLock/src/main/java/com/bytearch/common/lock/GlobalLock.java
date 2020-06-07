package com.bytearch.common.lock;

/**
 * @author iyw
 * Global lock for exclusive resources in distributed environment
 */
public interface GlobalLock {

    /**
     * Try to acquire the lock. If the lock has been acquired,
     * it can still be acquired in this call
     * @return
     */
    boolean lock();

    /**
     * Calling this method will release the lock on the resource whether or not the lock is acquired
     */
    void unlock();
}
