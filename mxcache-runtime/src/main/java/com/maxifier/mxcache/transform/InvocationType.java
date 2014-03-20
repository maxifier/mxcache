/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Opcodes;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public enum InvocationType {
    INTERFACE(Opcodes.INVOKEINTERFACE, true),
    VIRTUAL(Opcodes.INVOKEVIRTUAL, true),
    STATIC(Opcodes.INVOKESTATIC, false),
    KEY_INTERFACE(Opcodes.INVOKEINTERFACE, false),
    KEY_VIRTUAL(Opcodes.INVOKEVIRTUAL, false);

    private final int opcode;

    private final boolean needsField;

    InvocationType(int opcode, boolean needsField) {
        this.opcode = opcode;
        this.needsField = needsField;
    }

    public int getOpcode() {
        return opcode;
    }

    public boolean isNeedsField() {
        return needsField;
    }

    public boolean isRequiresKey() {
        return this == INTERFACE || this == VIRTUAL || this == STATIC;
    }

    public boolean isKeyInvocation() {
        return this == KEY_INTERFACE || this == KEY_VIRTUAL;
    }
}
