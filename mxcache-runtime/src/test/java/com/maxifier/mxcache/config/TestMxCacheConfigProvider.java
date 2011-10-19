package com.maxifier.mxcache.config;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 19.04.11
 * Time: 9:38
 */
public class TestMxCacheConfigProvider extends MxCacheConfigProviderImpl {
    private final MxCacheConfig bootstrapConfig;

    public TestMxCacheConfigProvider(MxCacheConfig bootstrapConfig) {
        super(false);
        this.bootstrapConfig = bootstrapConfig;
    }

    @Override
    MxCacheConfig loadBootstrapConfig() {
        return bootstrapConfig;
    }
}
