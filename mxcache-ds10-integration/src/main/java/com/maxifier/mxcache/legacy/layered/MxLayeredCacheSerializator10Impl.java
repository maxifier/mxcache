package com.maxifier.mxcache.legacy.layered;

import com.magenta.dataserializator.MxDataSerializator;
import com.magenta.dataserializator.MxObjectInput;
import com.magenta.dataserializator.MxObjectOutput;
import com.magenta.dataserializator.MxObjectSerializator;
import gnu.trove.THashMap;

import javax.annotation.Nonnull;

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
public class MxLayeredCacheSerializator10Impl implements MxLayeredCacheSerializator, MxObjectSerializator<MxLayeredCache> {
    private static final byte MX_CACHE_CODE = 40;

    private static final boolean SAFE_MODE = false;

    private final Map<String, MxLayeredCache> cacheRegistry;

    public MxLayeredCacheSerializator10Impl(MxDataSerializator dataSerializator) {
        cacheRegistry = new THashMap<String, MxLayeredCache>();
        dataSerializator.register(this, true);
    }

    @Override
    public synchronized void register(MxLayeredCache mxLayeredCache) {
        String name = mxLayeredCache.getName();
        if (cacheRegistry.put(name, mxLayeredCache) != null) {
            throw new IllegalArgumentException("Cache named " + name + " has already registered");
        }
    }

    @Override
    public byte getCode() {
        return MX_CACHE_CODE;
    }

    @Nonnull
    @Override
    public Class<?> getObjectClass() {
        return MxLayeredCache.class;
    }

    @Override
    public boolean isNeedLibrary() {
        return false;
    }

    @Override
    public void serialize(MxLayeredCache object, MxObjectOutput out) throws IOException {
        if (SAFE_MODE) {
            assert cacheRegistry.containsKey(object.getName());
        }
        out.writeObject(object.getName());
    }

    @Override
    public MxLayeredCache deserialize(MxObjectInput in) throws ClassNotFoundException, IOException {
        String name = (String) in.readObject();
        return cacheRegistry.get(name);
    }
}
