package com.maxifier.mxcache.legacy.layered;

import com.magenta.dataserializator.*;
import gnu.trove.THashMap;

import java.io.IOException;
import java.util.Map;

/**
 * Project: Smart Advertising
 * Created by: Yakoushin Andrey
 * Date: 03.04.2009
 * Time: 15:17:51
 * <p/>
 * Copyright (c) 1999-2008 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public class MxLayeredCacheSerializator11Impl implements MxLayeredCacheSerializator, MxSerializer<MxLayeredCache> {
    private static final long VERSION = 0;
    private static final long TYPE = 40;
    private static final long MX_CACHE_CODE = (VERSION << 32) | TYPE;

    private static final boolean SAFE_MODE = false;

    private final Map<String, MxLayeredCache> cacheRegistry;

    public MxLayeredCacheSerializator11Impl(MxDataSerializator dataSerializator) {
        cacheRegistry = new THashMap<String, MxLayeredCache>();
        dataSerializator.doSerialize(MxLayeredCache.class).byDefault().with(MX_CACHE_CODE, this);
    }

    @Override
    public synchronized void register(MxLayeredCache mxLayeredCache) {
        String name = mxLayeredCache.getName();
        if (cacheRegistry.put(name, mxLayeredCache) != null) {
            throw new IllegalArgumentException("Cache named " + name + " has already registered");
        }
    }

    @Override
    public boolean isNeedLibrary() {
        return false;
    }

    @Override
    public void serialize(MxObjectOutput out, long code, MxLayeredCache object) throws IOException {
        if (SAFE_MODE) {
            assert cacheRegistry.containsKey(object.getName());
        }
        out.writeObject(object.getName());
    }

    @Override
    public MxLayeredCache deserialize(MxObjectInput in, long code) throws ClassNotFoundException, IOException {
        String name = (String) in.readObject();
        return cacheRegistry.get(name);
    }
}
