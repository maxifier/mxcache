package com.maxifier.mxcache.legacy;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 08.11.2010
* Time: 16:46:22
*/
class MemoryStat {
    private static final Runtime RUNTIME = Runtime.getRuntime();

    private long maxMem;
    private long freeMem;
    private long events;

    public void measure() {
        maxMem += RUNTIME.maxMemory();
        freeMem += RUNTIME.freeMemory() + RUNTIME.maxMemory() - RUNTIME.totalMemory();
        events++;
    }

    public boolean isAverageFreeLessThan(long minFreeMem) {
        return freeMem < minFreeMem * events;
    }

    public void reset() {
        maxMem = 0;
        freeMem = 0;
        events = 0;
    }

    public long getAverageFree() {
        return freeMem / events;
    }

    public double getFreeRate() {
        return ((double) freeMem) / maxMem;
    }
}
