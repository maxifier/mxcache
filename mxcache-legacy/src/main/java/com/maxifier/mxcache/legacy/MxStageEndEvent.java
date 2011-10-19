package com.maxifier.mxcache.legacy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 07.07.2009
 * Time: 8:53:18
 */
public class MxStageEndEvent extends MxStageEvent {
    private final long duration;

    public MxStageEndEvent(MxStage stage, long time, long start) {
        super(stage, time);
        duration = time - start;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return getStage() + " : " + duration;
    }
}
