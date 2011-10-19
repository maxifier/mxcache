package com.maxifier.mxcache.legacy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 07.07.2009
 * Time: 9:09:27
 */
public class MxStageStartEvent extends MxStageEvent {
    public MxStageStartEvent(MxStage stage, long time) {
        super(stage, time);
    }

    @Override
    public String toString() {
        return getStage() + " : started";
    }
}
