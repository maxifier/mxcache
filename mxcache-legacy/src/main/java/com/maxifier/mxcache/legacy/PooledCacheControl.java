package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.util.FormatHelper;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 27.02.2009
 * Time: 10:23:19
 */
public class PooledCacheControl implements PooledCacheControlMBean {
    private final MxCachePoolManager manager;

    public PooledCacheControl(MxCachePoolManager manager) {
        this.manager = manager;
    }

    @Override
    public double getFreeVariation() {
        return manager.getConfiguration().getFreeVariation();
    }

    @Override
    public void setFreeVariation(double freeVariation) {
        manager.getConfiguration().setFreeVariation(freeVariation);
    }

    @Override
    public double getOptimalFree() {
        return manager.getConfiguration().getOptimalFree();
    }

    @Override
    public void setOptimalFree(double optimalFree) {
        manager.getConfiguration().setOptimalFree(optimalFree);
    }

    @Override
    public String getMinFreeMem() {
        return FormatHelper.formatSize(manager.getConfiguration().getMinFreeMem());
    }

    @Override
    public void setMinFreeMem(String minFreeMem) {
        manager.getConfiguration().setMinFreeMem((long) FormatHelper.parseSize(minFreeMem));
    }

    @Override
    public double getLimitValue() {
        return manager.getConfiguration().getLimit();
    }

    @Override
    public int getPeriod() {
        return manager.getConfiguration().getPeriod();
    }

    @Override
    public int getActivePeriods() {
        return manager.getActivePeriods();
    }

    @Override
    public String printConvertationStatistics() {
        return manager.getConverter().getStatistics();
    }

    @Override
    public void clearTo(double rate) {
        manager.getConfiguration().clearTo(rate);
    }

    @Override
    public void setLimit(double maxSize) {
        manager.getConfiguration().setLimit(maxSize);
    }

    @Override
    public double getPoolSize() {
        return manager.getConfiguration().getPoolSize();
    }

    @Override
    public void setLimitIncreaseScaler(double limitIncreaseScaler) {
        manager.getConfiguration().setLimitIncreaseScaler(limitIncreaseScaler);
    }

    @Override
    public void setMinRateToDecrease(double minRateToDecrease) {
        manager.getConfiguration().setMinRateToDecrease(minRateToDecrease);
    }

    @Override
    public void setLimitDecreaseScaler(double poolSizeDecrease) {
        manager.getConfiguration().setLimitDecreaseScaler(poolSizeDecrease);
    }

    @Override
    public double getLimitDecreaseScaler() {
        return manager.getConfiguration().getLimitDecreaseScaler();
    }

    @Override
    public double getMinLimitToDecrease() {
        return manager.getMinLimitToDecrease();
    }

    @Override
    public double getLimitIncreaseScaler() {
        return manager.getConfiguration().getLimitIncreaseScaler();
    }

    @Override
    public void setMinLimit(double minLimit) {
        manager.getConfiguration().setMinLimit(minLimit);
    }

    @Override
    public double getMinLimit() {
        return manager.getConfiguration().getMinLimit();
    }

    @Override
    public double getMinRateToDecrease() {
        return manager.getConfiguration().getMinRateToDecrease();
    }

    @Override
    public int getMisses() {
        return manager.getStatistics().getMisses();
    }

    @Override
    public int getHits() {
        return manager.getStatistics().getHits();
    }

    @Override
    public double getMissRate() {
        int misses = getMisses();
        int hits = getHits();
        return 100.0 * misses / (hits + misses);
    }

    @Override
    public int getYoungCount() {
        return manager.getYoungCount();
    }

    @Override
    public int getOldCount() {
        return manager.getOldCount();
    }
}
