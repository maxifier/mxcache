/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.instrumentation.CommonRuntimeTypes;
import com.maxifier.mxcache.util.MxField;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.*;

/**
 * This visitor wraps method code into resource access in the same way as if it was guarded with try/finally.
 *
 * Generates the same code as Javac usually does for try/finally statement.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class ResourceMethodVisitor extends MxGeneratorAdapter {
    private final ResourceMethodContext context;

    private List<Label> regions = new ArrayList<Label>();
    private Label finishLabel;

    public ResourceMethodVisitor(MethodVisitor visitor, int access, String name, String desc, Type thisClass, ResourceMethodContext context) {
        super(visitor, access, name, desc, thisClass);
        this.context = context;
    }

    @Override
    public void visitCode() {
        startResourceOperations();
        regions.add(mark());
    }

    @Override
    public void visitInsn(int opcode) {
        if (isExitInstruction(opcode)) {
            regions.add(mark());
            endResourceOperations();
            if (finishLabel == null) {
                finishLabel = new Label();
            }
            goTo(finishLabel);
            // do not include goto instruction into try/finally scope
            regions.add(mark());
        } else {
            super.visitInsn(opcode);
        }
    }

    private boolean isExitInstruction(int opcode) {
        switch (opcode) {
            case RETURN:
            case IRETURN:
            case FRETURN:
            case ARETURN:
            case LRETURN:
            case DRETURN:
                return true;
        }
        return false;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        Label handler = mark();
        regions.add(handler);

        int n = regions.size();
        if ((n & 1) != 0) {
            throw new IllegalStateException("Invalid region size: " + n);
        }
        for (int i = 0; i < n; i += 2) {
            Label start = regions.get(i);
            Label end = regions.get(i + 1);
            if (start.getOffset() != end.getOffset()) {
                visitTryCatchBlock(start, end, handler, null);
            }
        }
        int local = newLocal(CommonRuntimeTypes.THROWABLE_TYPE);
        storeLocal(local);
        endResourceOperations();
        loadLocal(local);
        // yes, call super cause we don't want resources to be closed twice.
        super.visitInsn(ATHROW);

        // if there were no normal exits (i.e. only "throw smth" then we don't need to add an extra return statement
        // at the end of method.
        if (finishLabel != null) {
            mark(finishLabel);
            super.returnValue();
        }

        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (InstrumentatorImpl.RESOURCE_READER_DESCRIPTOR.equals(desc) || InstrumentatorImpl.RESOURCE_WRITER_DESCRIPTOR.equals(desc)) {
            // filter @ResReader & @ResWriter to avoid duplicating instrumentation
            return null;
        }
        return super.visitAnnotation(desc, visible);
    }

    private void startResourceOperations() {
        process(READ_START, context.getReadResourceOrder());
        process(WRITE_START, context.getWriteResourceOrder());
    }

    private void endResourceOperations() {
        process(READ_END, context.getReadResourceOrder());
        process(WRITE_END, context.getWriteResourceOrder());
    }

    private void process(Method method, List<MxField> fields) {
        for (MxField field : fields) {
            get(field);
            invokeInterface(MX_RESOURCE_TYPE, method);
        }
    }
}
