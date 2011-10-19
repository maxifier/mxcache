package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.commons.EmptyVisitor;

/**
 * Created by IntelliJ IDEA.
* User: dalex
* Date: 15.04.2010
* Time: 13:10:02
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
