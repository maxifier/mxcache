/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.ClassReader;
import com.maxifier.mxcache.asm.ClassWriter;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class SmartClassWriter extends ClassWriter {
    public SmartClassWriter(ClassReader classReader) {
        super(classReader, COMPUTE_FRAMES);
    }

    public SmartClassWriter(int flags) {
        super(flags);
    }

    public SmartClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        Class<?> c, d;
        try {
            c = Class.forName(type1.replace('/', '.'));
            d = Class.forName(type2.replace('/', '.'));
        } catch (Exception e) {
            return "java/lang/Object";
        }
        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }
}
