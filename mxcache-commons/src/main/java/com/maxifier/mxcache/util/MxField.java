package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.Type;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 03.03.11
 * Time: 17:25
 */
public class MxField {
    private final int access;
    private final Type owner;
    private final String name;
    private final Type type;

    public MxField(int access, Type owner, String name, Type type) {
        this.access = access;
        this.owner = owner;
        this.name = name;
        this.type = type;
    }

    public int getAccess() {
        return access;
    }

    public Type getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
