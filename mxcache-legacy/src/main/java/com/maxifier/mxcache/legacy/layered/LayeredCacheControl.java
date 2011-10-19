package com.maxifier.mxcache.legacy.layered;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 04.03.2009
 * Time: 14:45:09
 */
public class LayeredCacheControl implements LayeredCacheControlMBean {
    private final MxLayeredCache manager;

    public LayeredCacheControl(MxLayeredCache manager) {
        this.manager = manager;
    }

    @Override
    public int getHits() {
        return manager.getHits();
    }

    @Override
    public int getMisses() {
        return manager.getMisses();
    }

    @Override
    public double getMissRate() {
        return 100.0 * manager.getMisses() / (manager.getHits() + manager.getMisses());
    }

    @Override
    public String printConvertationStatistics() {
        return manager.getConverter().getStatistics();
    }

    @Override
    public String printKeyStatistics() {
        return manager.printKeyStatistics();
    }
}
