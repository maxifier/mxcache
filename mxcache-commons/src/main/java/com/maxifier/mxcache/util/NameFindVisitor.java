/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.commons.EmptyVisitor;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class NameFindVisitor extends EmptyVisitor {
    private String name;

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
