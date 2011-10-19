package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.commons.Method;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 19.10.2010
 * Time: 13:37:33
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
