package com.maxifier.mxcache.legacy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 27.02.2009
 * Time: 9:30:45
 */
public interface PooledCacheControlMBean extends AbstractCacheControl {
    @Override
    String printConvertationStatistics();

    @Override
    int getMisses();

    @Override
    int getHits();

    @Override
    double getMissRate();

    double getLimitValue();

    int getPeriod();

    int getActivePeriods();

    void clearTo(double rate);

    void setLimit(double maxSize);

    double getPoolSize();

    double getFreeVariation();

    void setFreeVariation(double freeVariation);

    double getOptimalFree();

    void setOptimalFree(double optimalFree);

    String getMinFreeMem();

    void setMinFreeMem(String minFreeMem);

    void setLimitIncreaseScaler(double limitIncreaseScaler);

    void setMinRateToDecrease(double minRateToDecrease);

    void setLimitDecreaseScaler(double poolSizeDecrease);

    void setMinLimit(double minLimit);

    double getLimitDecreaseScaler();

    double getMinLimitToDecrease();

    double getLimitIncreaseScaler();

    double getMinLimit();

    double getMinRateToDecrease();

    int getYoungCount();

    int getOldCount();
}
