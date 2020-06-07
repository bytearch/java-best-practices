package com.bytearch.common.lock;

/**
 * @author yarw
 */
public class DistributedlLockUtil implements GlobalLock {
    private static final Object lockValue = new Object();
    protected int invalidSecond = 60;
    protected String lockKey;
    protected boolean isLocked = false;

    @Override
    public boolean lock() {
        return false;
    }

    @Override
    public void unlock() {

    }
}
