/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import gnu.trove.THashMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import static com.maxifier.mxcache.asm.Type.getType;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class CommonRuntimeTypes {
    public static final Type LIST_TYPE = Type.getType(List.class);
    public static final Type MAP_TYPE = getType(Map.class);
    public static final Type ILLEGAL_ARGUMENT_EXCEPTION_TYPE = Type.getType(IllegalArgumentException.class);
    public static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
    public static final Type OBJECT_INPUT_STREAM_TYPE = Type.getType(ObjectInputStream.class);
    public static final Type CLASS_NOT_FOUND_EXCEPTION_TYPE = Type.getType(ClassNotFoundException.class);
    public static final Type IO_EXCEPTION_TYPE = Type.getType(IOException.class);
    public static final Type THASHMAP_TYPE = Type.getType(THashMap.class);

    @SuppressWarnings({ "PublicStaticArrayField" })
    public static final String[] READ_OBJECT_EXCEPTIONS = { CLASS_NOT_FOUND_EXCEPTION_TYPE.getInternalName(), IO_EXCEPTION_TYPE.getInternalName() };

    public static final Method LIST_ADD_METHOD = Method.getMethod("boolean add(Object)");
    public static final Method READ_OBJECT_METHOD = Method.getMethod("void readObject(java.io.ObjectInputStream)");
    public static final Method DEFAULT_READ_OBJECT_METHOD = Method.getMethod("void defaultReadObject()");

    private CommonRuntimeTypes() {
    }
}
