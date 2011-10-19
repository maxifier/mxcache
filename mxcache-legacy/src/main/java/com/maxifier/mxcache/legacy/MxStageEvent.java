package com.maxifier.mxcache.legacy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 07.07.2009
 * Time: 9:10:49
 */
public abstract class MxStageEvent {
    private final MxStage stage;
    private final long time;

    public MxStageEvent(MxStage stage, long time) {
        this.stage = stage;
        this.time = time;
    }

    public MxStage getStage() {
        return stage;
    }

    public long getTime() {
        return time;
    }
}
