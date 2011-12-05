package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.legacy.MxResource;
import com.maxifier.mxcache.legacy.MxResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 11:10:58
 */
public abstract class MxAbstractResourceConverter<T> implements MxConverter<T, MxResource> {
    private static final Random RANDOM = new Random();

    private static final String ID_TAG_NAME = "${id}";
    private static final String CLASS_ABBR_TAG = "${classAbbr}";

    private static final String DEFAULT_RESOURCE_NAME_TEMPLATE = "cache/${classAbbr}_${id}.tmp";

    private static final int MAX_ABBREVIATION_LENGTH = 3;
    private static final int MAX_CACHE_RANDOM_ID = 0x1000000;

    private final MxResourceManager resourceManager;
    private final String resourceNameTemplate;

    public MxAbstractResourceConverter(MxResourceManager resourceManager, @Nullable String resourceNameTemplate) {
        this.resourceManager = resourceManager;
        if (resourceNameTemplate == null) {
            this.resourceNameTemplate = DEFAULT_RESOURCE_NAME_TEMPLATE;
        } else {
            if (!resourceNameTemplate.contains(ID_TAG_NAME)) {
                throw new IllegalArgumentException("No ${id} tag in resource name template");
            }
            this.resourceNameTemplate = resourceNameTemplate;
        }
    }

    public MxAbstractResourceConverter(MxResourceManager resourceManager) {
        this(resourceManager, null);
    }

    private static String getClassAbr(@NotNull Class cls) {
        String s = cls.getName();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length() && b.length() <= MAX_ABBREVIATION_LENGTH; i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c) || Character.isDigit(c)) {
                b.append(Character.toLowerCase(c));
            }
        }
        return b.toString();
    }

    protected MxResource createUniqueCacheFile(@NotNull Class cls) {
        synchronized (RANDOM) {
            MxResource cacheFile;
            String resourceName = generateName(cls);
            int index = resourceName.indexOf(ID_TAG_NAME);
            String prefix = resourceName.substring(0, index);
            String postfix = resourceName.substring(index + ID_TAG_NAME.length());
            do {
                cacheFile = resourceManager.getTempResource(prefix + Integer.toHexString(RANDOM.nextInt(MAX_CACHE_RANDOM_ID)) + postfix);
            } while (cacheFile.exists());
            try {
                cacheFile.deleteOnExit();
            } catch (Exception e) {
                // if vm shutdown started, cacheFile.deleteOnExit() may fail with NullPointerException
                // we ignore such exceptions
            }
            return cacheFile;
        }
    }

    private String generateName(Class cls) {
        return resourceNameTemplate.replace(CLASS_ABBR_TAG, getClassAbr(cls));
    }
}
