package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.util.Generator;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 05.03.2010
 * Time: 15:37:58
 */
public interface Context {
    void define(Type type, byte[] byteCode);

    void innerClass(Type innerType);

    void addInstanceInitializer(Generator g);

    void addStaticInitializer(Generator g);

    void registerCache(String fieldName, Type cacheType, Type returnType, Type keyType, Type calculable, Generator lockGetter);
}
