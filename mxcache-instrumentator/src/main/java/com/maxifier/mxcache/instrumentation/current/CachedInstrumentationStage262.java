/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.AnnotationVisitor;
import com.maxifier.mxcache.asm.ClassVisitor;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.asm.commons.AdviceAdapter;
import com.maxifier.mxcache.instrumentation.CommonRuntimeTypes;
import com.maxifier.mxcache.instrumentation.IllegalCachedClass;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import java.lang.reflect.Modifier;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.DEFAULT_READ_OBJECT_METHOD;
import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.READ_OBJECT_EXCEPTIONS;
import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.READ_OBJECT_METHOD;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.*;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.GET_DEFAULT_CONTEXT;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.REGISTER_CACHE_METHOD;

/**
 * CachedInstrumentationStage262
 *
 * @author Azat Abdulvaliev (azat.abdulvaliev@maxifier.com) (2015-12-03 15:31)
 */
public class CachedInstrumentationStage262 extends CachedInstrumentationStage {
    CachedInstrumentationStage262(InstrumentatorImpl instrumentator, ClassVisitor classWriter, ClassVisitor nextDetector) {
        super(instrumentator, classWriter, nextDetector);
    }

    @Override
    protected void generateRegisterCache() {
        MxGeneratorAdapter iim = createInitializerMethod(ACC_PRIVATE | ACC_SYNTHETIC, REGISTER_CACHE_METHOD);
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
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitMethodInsn(INVOKESTATIC, CACHE_FACTORY_TYPE.getInternalName(), GET_CONTEXT_FROM_STREAM.getName(), GET_CONTEXT_FROM_STREAM.getDescriptor());
        visitor.visitMethodInsn(INVOKEVIRTUAL, getThisType().getInternalName(), REGISTER_CACHE_METHOD.getName(), REGISTER_CACHE_METHOD.getDescriptor());
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitMethodInsn(INVOKEVIRTUAL, CommonRuntimeTypes.OBJECT_INPUT_STREAM_TYPE.getInternalName(), DEFAULT_READ_OBJECT_METHOD.getName(), DEFAULT_READ_OBJECT_METHOD.getDescriptor());
        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    @Override
    protected MethodVisitor createRegistrator(int access, String name, String desc, MethodVisitor oldVisitor) {
        return new CacheRegistrator(oldVisitor, access, name, desc);
    }

    @Override
    protected CachedMethodVisitor createMethodVisitor(int access, String name, String desc, String sign, String[] exceptions, MethodVisitor oldVisitor, CachedMethodContext context) {
        return new CachedMethodVisitor(this, oldVisitor, access, name, desc, sign, exceptions, getThisType(), context, !Modifier.isStatic(access), true);
    }

    private class CacheRegistrator extends AdviceAdapter {
        private int contextIndex = -1;

        public CacheRegistrator(MethodVisitor oldVisitor, int access, String name, String desc) {
            super(Opcodes.ASM5, oldVisitor, access, name, desc);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
            if (desc.equals(RuntimeTypes.USE_CACHE_CONTEXT_TYPE.getDescriptor())) {
                if (contextIndex >= 0) {
                    throw new IllegalCachedClass("Multiple @UseCacheContext annotations", getSourceFileName());
                }
                contextIndex = parameter;
            }
            return super.visitParameterAnnotation(parameter, desc, visible);
        }

        @Override
        protected void onMethodEnter() {
            loadThis();
            if (contextIndex < 0) {
                invokeStatic(CACHE_FACTORY_TYPE, GET_DEFAULT_CONTEXT);
            } else {
                loadArg(contextIndex);
            }
            invokeVirtual(getThisType(), REGISTER_CACHE_METHOD);
        }
    }
}