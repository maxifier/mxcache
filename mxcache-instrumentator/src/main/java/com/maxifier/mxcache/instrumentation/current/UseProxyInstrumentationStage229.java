/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.instrumentation.IllegalCachedClass;
import com.maxifier.mxcache.asm.AnnotationVisitor;
import com.maxifier.mxcache.asm.ClassVisitor;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.commons.AdviceAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.*;
import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.DEFAULT_READ_OBJECT_METHOD;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class UseProxyInstrumentationStage229 extends UseProxyInstrumentationStage {
    public UseProxyInstrumentationStage229(InstrumentatorImpl instrumentator, ClassVisitor cv, ClassVisitor nextDetector) {
        super(instrumentator, cv, nextDetector);
    }

    @Override
    protected void generateReadObject() {
        MethodVisitor visitor = super.visitMethod(ACC_PRIVATE | ACC_SYNTHETIC, READ_OBJECT_METHOD.getName(), READ_OBJECT_METHOD.getDescriptor(), null, READ_OBJECT_EXCEPTIONS);
        visitor.visitCode();
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitMethodInsn(INVOKESTATIC, CACHE_FACTORY_TYPE.getInternalName(), GET_CONTEXT_FROM_STREAM.getName(), GET_CONTEXT_FROM_STREAM.getDescriptor());
        visitor.visitMethodInsn(INVOKEVIRTUAL, getThisType().getInternalName(), INIT_PROXY_FACTORIES_METHOD.getName(), INIT_PROXY_FACTORIES_METHOD.getDescriptor());
        visitor.visitVarInsn(ALOAD, 1);
        visitor.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INPUT_STREAM_TYPE.getInternalName(), DEFAULT_READ_OBJECT_METHOD.getName(), DEFAULT_READ_OBJECT_METHOD.getDescriptor());
        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    @Override
    protected void generateInitProxyFactoriesStaticMethod() {
        MxGeneratorAdapter initStatic = new MxGeneratorAdapter(ACC_PRIVATE | ACC_STATIC, INIT_PROXY_FACTORIES_METHOD, super.visitMethod(ACC_PRIVATE | ACC_STATIC, INIT_PROXY_FACTORIES_STATIC_METHOD.getName(), INIT_PROXY_FACTORIES_STATIC_METHOD.getDescriptor(), null, null), getThisType());
        initStatic.visitCode();
        int proxyManager = initStatic.newLocal(PROXY_MANAGER_TYPE);
        initStatic.invokeStatic(PROXY_MANAGER_TYPE, GET_PROXY_MANAGER_INSTANCE_METHOD);
        initStatic.storeLocal(proxyManager);
        for (ProxiedMethodContext context : getDetector().getProxiedMethods().values()) {
            initStatic.push(getThisType());
            initStatic.push(context.getId());
            Method method = context.getMethod();
            initStatic.push(method.getName());
            initStatic.push(method.getDescriptor());
            initStatic.push(context.isStatic());
            initStatic.invokeStatic(RESOLVABLE_GENERATOR_TYPE, GENERATE_RESOLVABLE);

            if (context.isStatic()) {
                initStatic.loadLocal(proxyManager);
                initStatic.invokeStatic(CACHE_FACTORY_TYPE, GET_DEFAULT_CONTEXT);
                initStatic.push(getThisType());
                initStatic.push(method.getName());
                initStatic.push(method.getDescriptor());
                initStatic.invokeVirtual(PROXY_MANAGER_TYPE, GET_PROXY_FACTORY_METHOD);
                initStatic.putStatic(getThisType(), PROXY_FACTORY_FIELD_PREFIX + context.getId(), PROXY_FACTORY_TYPE);
            }
        }
        initStatic.returnValue();
        initStatic.endMethod();
    }

    @Override
    protected void generateInitProxyFactoriesMethod() {
        MxGeneratorAdapter init = new MxGeneratorAdapter(ACC_PRIVATE, INIT_PROXY_FACTORIES_METHOD, super.visitMethod(ACC_PRIVATE, INIT_PROXY_FACTORIES_METHOD.getName(), INIT_PROXY_FACTORIES_METHOD.getDescriptor(), null, null), getThisType());
        init.visitCode();
        int proxyManager = init.newLocal(PROXY_MANAGER_TYPE);
        init.invokeStatic(PROXY_MANAGER_TYPE, GET_PROXY_MANAGER_INSTANCE_METHOD);
        init.storeLocal(proxyManager);
        for (ProxiedMethodContext context : getDetector().getProxiedMethods().values()) {
            if (!context.isStatic()) {
                init.loadThis();
                init.loadLocal(proxyManager);
                init.loadArg(0);
                init.push(getThisType());
                Method method = context.getMethod();
                init.push(method.getName());
                init.push(method.getDescriptor());
                init.invokeVirtual(PROXY_MANAGER_TYPE, GET_PROXY_FACTORY_METHOD);
                init.putField(getThisType(), PROXY_FACTORY_FIELD_PREFIX + context.getId(), PROXY_FACTORY_TYPE);
            }
        }
        init.returnValue();
        init.endMethod();
    }

    @Override
    protected MethodVisitor createProxyFactoryInitializer(int access, String name, String desc, MethodVisitor oldVisitor) {
        return new ProxyFactoryInitializer(oldVisitor, access, name, desc);
    }

    private class ProxyFactoryInitializer extends AdviceAdapter {
        private int contextIndex = -1;

        public ProxyFactoryInitializer(MethodVisitor oldVisitor, int access, String name, String desc) {
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
            invokeConstructor(getThisType(), INIT_PROXY_FACTORIES_METHOD);
        }
    }
}
