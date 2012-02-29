package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.resource.MxResource;
import gnu.trove.THashMap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.maxifier.mxcache.config.ResourceConfig;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 12.04.2010
 * Time: 9:18:30
 */
public final class MxResourceFactory {
    private static final Map<String, MxResource> RESOURCES = new THashMap<String, MxResource>();

    static {
        for (ResourceConfig resourceConfig : CacheFactory.getConfiguration().getResources()) {
            String name = resourceConfig.getName();
            RESOURCES.put(name, new MxStaticResource(name));
        }
    }

    private MxResourceFactory() {
    }

    /**
     * Гарантируется, что для одного имени всегда возвращается один и тот же ресурс.
     * Настройки ресурса будут загружены из конфигурации или использованы значения по умолчанию.
     * @param id имя ресурса
     * @return ресурс с заданным именем
     */
    public static synchronized MxResource getResource(@NotNull String id) {
        MxResource resource = RESOURCES.get(id);
        if (resource == null) {
            resource = new MxStaticResource(id);
            RESOURCES.put(id, resource);
        }
        return resource;
    }

    public static MxResource createResource(@NotNull Object owner, @NotNull String id) {
        return new MxBoundResource(owner, id);
    }

    public static synchronized Set<MxResource> getAllResources() {
        return Collections.unmodifiableSet(new THashSet<MxResource>(RESOURCES.values()));
    }
}
