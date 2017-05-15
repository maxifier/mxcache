/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.ClassVisitor;
import com.maxifier.mxcache.asm.Opcodes;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class NameFindVisitor extends ClassVisitor {
    private String name;

    NameFindVisitor() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
