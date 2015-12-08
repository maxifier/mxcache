/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.commons.AdviceAdapter;
import com.maxifier.mxcache.instrumentation.CommonRuntimeTypes;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.*;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class CachedInstrumentationStage219 extends CachedInstrumentationStage {
    CachedInstrumentationStage219(InstrumentatorImpl instrumentator, ClassVisitor classWriter, ClassVisitor nextDetector) {
        super(instrumentator, classWriter, nextDetector);
    }

    @Override
    protected void generateRegisterCache() {
        MxGeneratorAdapter iim = createInitializerMethod(ACC_PRIVATE | ACC_SYNTHETIC, REGISTER_CACHE_OLD_METHOD);
        iim.loadThis();
        iim.push(getThisType());
        iim.invokeStatic(CACHE_FACTORY_TYPE, FACTORY_REGISTER_INSTANCE_METHOD);
        applyInstanceInitializers(iim);
        iim.returnValue();
        iim.endMethod();
    }

    @Override
    protected void generateReadObject() {
        MethodVisitor visitor = visitTransparentMethod(ACC_PRIVATE | ACC_SYNTHETIC, READ_OBJECT_METHOD.getName(), READ_OBJECT_METHOD.getDescriptor(), null, READ_OBJECT_EXCEPTIONS);
        visitor.visitCode();
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitMethodInsn(INVOKEVIRTUAL, getThisType().getInternalName(), REGISTER_CACHE_OLD_METHOD.getName(), REGISTER_CACHE_OLD_METHOD.getDescriptor());
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitMethodInsn(INVOKEVIRTUAL, CommonRuntimeTypes.OBJECT_INPUT_STREAM_TYPE.getInternalName(), DEFAULT_READ_OBJECT_METHOD.getName(), DEFAULT_READ_OBJECT_METHOD.getDescriptor());
        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    @Override
    protected CachedMethodVisitor createMethodVisitor(int access, String name, String desc, String sign, String[] exceptions, MethodVisitor oldVisitor, CachedMethodContext context) {
        return new CachedMethodVisitor(this, oldVisitor, access, name, desc, sign, exceptions, getThisType(), context, false, false);
    }

    @Override
    protected MethodVisitor createRegistrator(int access, String name, String desc, MethodVisitor oldVisitor) {
        return new CacheRegistrator(oldVisitor, access, name, desc);
    }

    private class CacheRegistrator extends AdviceAdapter {
        public CacheRegistrator(MethodVisitor oldVisitor, int access, String name, String desc) {
            super(Opcodes.ASM4, oldVisitor, access, name, desc);
        }

        @Override
        protected void onMethodEnter() {
            loadThis();
            invokeVirtual(getThisType(), REGISTER_CACHE_OLD_METHOD);
        }
    }
}
