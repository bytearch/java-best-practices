package com.bytearch.sequence.util;

/**
 * @author yarw
 */
public class IdEntity {
    private long createTime;
    private long workerId;
    private long extraId;
    private long sequenceId;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public long getExtraId() {
        return extraId;
    }

    public void setExtraId(long extraId) {
        this.extraId = extraId;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public String toString() {
        return "IdEntity{" +
                "createTime=" + createTime +
                ", workerId=" + workerId +
                ", extraId=" + extraId +
                ", sequenceId=" + sequenceId +
                '}';
    }
}
