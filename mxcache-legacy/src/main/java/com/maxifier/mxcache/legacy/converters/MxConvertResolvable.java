package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.legacy.MxCacheProxy;
import com.maxifier.mxcache.proxy.Resolvable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 06.02.2009
 * Time: 10:31:38
 */
public class MxConvertResolvable<T> implements MxConverter<Resolvable<T>, T> {
    @SuppressWarnings({ "unchecked" })
    @Override
    public T convert(Resolvable<T> t) {
        T value = t.getValue();
        while(value instanceof MxCacheProxy) {
            value = ((MxCacheProxy<T>)value).getValue();
        }
        return value;
    }
}
