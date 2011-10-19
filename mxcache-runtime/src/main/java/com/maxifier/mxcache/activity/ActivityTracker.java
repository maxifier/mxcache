package com.maxifier.mxcache.activity;

import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 12:10:13
 */
public final class ActivityTracker {
    private static final Map<String, Activity> ACTIVITIES = new THashMap<String, Activity>();

//    static {
//        for (ResourceConfig resourceConfig : MxCacheConfig.getInstance().getResources()) {
//            String name = resourceConfig.getName();
//            ACTIVITIES.put(name, new ActivityImpl(name));
//        }
//    }

    private ActivityTracker() {
    }

    /**
     * Гарантируется, что для одного имени всегда возвращается один и тот же ресурс.
     * Настройки ресурса будут загружены из конфигурации или использованы значения по умолчанию.
     *
     * @param id имя ресурса
     *
     * @return ресурс с заданным именем
     */
    public static synchronized Activity getActivity(@NotNull String id) {
        Activity resource = ACTIVITIES.get(id);
        if (resource == null) {
            resource = new ActivityImpl(id);
            ACTIVITIES.put(id, resource);
        }
        return resource;
    }
}
