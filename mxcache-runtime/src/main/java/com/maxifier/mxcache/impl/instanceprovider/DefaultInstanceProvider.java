package com.maxifier.mxcache.impl.instanceprovider;

import com.maxifier.mxcache.InstanceProvider;
import com.maxifier.mxcache.NoSuchInstanceException;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.09.2010
 * Time: 9:08:53
 */
public final class DefaultInstanceProvider implements InstanceProvider {
    private static final DefaultInstanceProvider INSTANCE = new DefaultInstanceProvider();

    private final Map<Class, Provider> registry = Collections.synchronizedMap(new THashMap<Class, Provider>());

    private DefaultInstanceProvider() {}

    public static DefaultInstanceProvider getInstance() {
        return INSTANCE;
    }

    public <T> Binder<T> bind(final Class<T> t) {
        return new BinderImpl<T>(this, registry, t);
    }

    public void clearBinding(Class cls) {
        registry.remove(cls);
    }

    @NotNull
    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> T forClass(@NotNull Class<T> cls) {
        Provider provider = registry.get(cls);
        if (provider == null) {
            return createInstance(cls);
        }
        return (T) provider.get();
    }

    private static <T> T createInstance(@NotNull Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (InstantiationException e) {
            throw new NoSuchInstanceException(e);
        } catch (IllegalAccessException e) {
            throw new NoSuchInstanceException(e);
        }
    }

}
