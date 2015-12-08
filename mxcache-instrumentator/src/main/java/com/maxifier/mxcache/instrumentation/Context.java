/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.util.Generator;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Context {
    void define(Type type, byte[] byteCode);

    void innerClass(Type innerType);

    void addInstanceInitializer(Generator g);

    void addStaticInitializer(Generator g);

    void registerCache(String cacheFieldName, String hashingStratsFieldName, Type cacheType, Type returnType, Type keyType, Type calculable, Generator lockGetter);
}
