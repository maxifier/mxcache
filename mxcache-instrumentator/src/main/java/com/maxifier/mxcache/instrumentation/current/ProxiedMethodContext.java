/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.commons.Method;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class ProxiedMethodContext {
    private final int id;

    private final boolean isStatic;

    private final Method method;

    public ProxiedMethodContext(int id, boolean isStatic, Method method) {
        this.id = id;
        this.isStatic = isStatic;
        this.method = method;
    }

    public int getId() {
        return id;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Method getMethod() {
        return method;
    }
}
