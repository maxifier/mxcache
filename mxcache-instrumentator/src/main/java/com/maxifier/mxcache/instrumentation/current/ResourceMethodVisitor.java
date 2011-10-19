package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.AnnotationVisitor;
import com.maxifier.mxcache.instrumentation.CommonRuntimeTypes;
import com.maxifier.mxcache.asm.Label;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import static com.maxifier.mxcache.asm.Opcodes.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 12.04.2010
 * Time: 12:49:37
 */
class ResourceMethodVisitor extends MxGeneratorAdapter {
    private static final String NO_ARG_VOID = "()V";

    private final ResourceMethodContext context;

    private final Type thisClass;

    private Label start;

    public ResourceMethodVisitor(MethodVisitor visitor, int access, String name, String desc, Type thisClass, ResourceMethodContext context) {
        super(visitor, access, name, desc, thisClass);
        this.thisClass = thisClass;
        this.context = context;
    }

    @Override
    public void visitCode() {
        startResourceOperations();
        start = mark();
    }

    @Override
    public void visitInsn(int opcode) {
        if (isExitInstruction(opcode)) {
            endResourceOperations();
        }
        super.visitInsn(opcode);
    }

    private boolean isExitInstruction(int opcode) {
        switch (opcode) {
            case RETURN:
            case IRETURN:
            case FRETURN:
            case ARETURN:
            case LRETURN:
            case DRETURN:
            case ATHROW:
                return true;
        }
        return false;
    }

    @Override
    public void visitEnd() {
        Label end = mark();
        catchException(start, end, null);
        int local = newLocal(CommonRuntimeTypes.THROWABLE_TYPE);
        storeLocal(local);
        endResourceOperations();
        loadLocal(local);
        // yes, call super cause we don't want resources to be closed twice.
        super.visitInsn(ATHROW);
        super.visitEnd();
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
        for (String resourceField : context.getReadResourceOrder()) {
            visitFieldInsn(GETSTATIC, thisClass.getInternalName(), resourceField, RuntimeTypes.MX_RESOURCE_TYPE.getDescriptor());
            visitMethodInsn(INVOKEINTERFACE, RuntimeTypes.MX_RESOURCE_TYPE.getInternalName(), "readStart", NO_ARG_VOID);
        }
        for (String resourceField : context.getWriteResourceOrder()) {
            visitFieldInsn(GETSTATIC, thisClass.getInternalName(), resourceField, RuntimeTypes.MX_RESOURCE_TYPE.getDescriptor());
            visitMethodInsn(INVOKEINTERFACE, RuntimeTypes.MX_RESOURCE_TYPE.getInternalName(), "writeStart", NO_ARG_VOID);
        }
    }

    private void endResourceOperations() {
        for (String resourceField : context.getReadResourceOrder()) {
            visitFieldInsn(GETSTATIC, thisClass.getInternalName(), resourceField, RuntimeTypes.MX_RESOURCE_TYPE.getDescriptor());
            visitMethodInsn(INVOKEINTERFACE, RuntimeTypes.MX_RESOURCE_TYPE.getInternalName(), "readEnd", NO_ARG_VOID);
        }
        for (String resourceField : context.getWriteResourceOrder()) {
            visitFieldInsn(GETSTATIC, thisClass.getInternalName(), resourceField, RuntimeTypes.MX_RESOURCE_TYPE.getDescriptor());
            visitMethodInsn(INVOKEINTERFACE, RuntimeTypes.MX_RESOURCE_TYPE.getInternalName(), "writeEnd", NO_ARG_VOID);
        }
    }
}
