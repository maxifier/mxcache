package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.EvictionPolicyEnum;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.StrategyProperty;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.03.11
 * Time: 14:38
 */
public final class ConfigurationParser {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationParser.class);

    private static final StrategyProperty<Integer> MAX_ELEMENTS_PROPERTY = StrategyProperty.create("ehcache.maxElements", int.class, UseEhcache.class, "maxElements");
    private static final StrategyProperty<EvictionPolicyEnum> MEM_EVICTION_POLICY = StrategyProperty.create("ehcache.memory.eviction-policy", EvictionPolicyEnum.class, EvictionPolicyEnum.DEFAULT, UseEhcache.class, "memoryEvictionPolicy");

    private static final Map<EvictionPolicyEnum, MemoryStoreEvictionPolicy> MAPPING;

    static {
        MAPPING = new EnumMap<EvictionPolicyEnum, MemoryStoreEvictionPolicy>(EvictionPolicyEnum.class);
        MAPPING.put(EvictionPolicyEnum.LRU, MemoryStoreEvictionPolicy.LRU);
        MAPPING.put(EvictionPolicyEnum.LFU, MemoryStoreEvictionPolicy.LFU);
        MAPPING.put(EvictionPolicyEnum.FIFO, MemoryStoreEvictionPolicy.FIFO);
    }

    private ConfigurationParser() {}

    public static CacheConfiguration parseConfiguration(CacheDescriptor<?> descriptor) {
        CacheConfiguration configuration = new CacheConfiguration(descriptor.getCacheName(), descriptor.getProperty(MAX_ELEMENTS_PROPERTY));
        EvictionPolicyEnum memEvictionPolicy = descriptor.getProperty(MEM_EVICTION_POLICY);
        if (memEvictionPolicy != EvictionPolicyEnum.DEFAULT) {
            MemoryStoreEvictionPolicy policy = MAPPING.get(memEvictionPolicy);
            if (policy == null) {
                logger.error("Unsupported eviction policy for ehcache: " + memEvictionPolicy + " at " + descriptor + ", ignoring");
            } else {
                configuration.memoryStoreEvictionPolicy(policy);
            }
        }
        return configuration;
    }
}
