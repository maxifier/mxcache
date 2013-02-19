package com.maxifier.mxcache.proxy;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;

import static com.maxifier.mxcache.util.CodegenHelper.EMPTY_TYPES;
import static com.maxifier.mxcache.util.CodegenHelper.OBJECT_TYPE;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 21.12.2009
 * Time: 13:02:32
 */
public class MxAbstractProxyFactory {
    protected static final String GETTER_NAME = "getValue";
    protected static final String VALUE_FIELD_NAME = "value";
    protected static final Type RESOLVABLE_TYPE = Type.getType(Resolvable.class);
    protected static final Method GETTER = new Method(GETTER_NAME, OBJECT_TYPE, EMPTY_TYPES);

    static {
        enshureCompatibleMxContainer();
    }

    protected static void enshureCompatibleMxContainer() {
        try {
            if (!Resolvable.class.getMethod(GETTER_NAME).getReturnType().equals(Object.class)) {
                throw new IllegalStateException("MxContainer." + GETTER_NAME + " should have Object return type");
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(Resolvable.class + " should have method getValue()");
        }
    }

    protected static String createProxyClassName(Class sourceClass) {
        return Type.getInternalName(sourceClass) + "$MxProxy";
    }
}
