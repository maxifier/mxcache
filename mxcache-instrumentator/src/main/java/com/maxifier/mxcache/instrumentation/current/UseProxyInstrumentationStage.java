/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.instrumentation.IllegalCachedClass;
import com.maxifier.mxcache.instrumentation.ClassDefinition;
import com.maxifier.mxcache.instrumentation.InstrumentationStage;
import com.maxifier.mxcache.asm.commons.AdviceAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.proxy.ResolvableGenerator;
import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.*;
import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.asm.Type.*;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.*;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
abstract class UseProxyInstrumentationStage extends ClassVisitor implements InstrumentationStage {

    protected static final String PROXY_FACTORY_FIELD_PREFIX = "$proxyFactory$";

    private final UseProxyDetector detector;

    private final InstrumentatorImpl instrumentator;

    private Type thisType;

    private boolean hasStaticInitializer;

    private boolean hasReadObject;

    private String sourceFileName;

    public UseProxyInstrumentationStage(InstrumentatorImpl instrumentator, ClassVisitor cv, ClassVisitor nextDetector) {
        super(Opcodes.ASM4, cv);
        this.instrumentator = instrumentator;
        detector = new UseProxyDetector(nextDetector);
    }

    @Override
    public boolean isClassChanged() {
        return detector.hasProxiedMethods();
    }

    @Override
    public UseProxyDetector getDetector() {
        return detector;
    }

    @Override
    public List<ClassDefinition> getAdditionalClasses() {
        return Collections.emptyList();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        thisType = Type.getObjectType(name);

        if (detector.hasProxiedMethods()) {
            addMarkerAnnotation();
        }
    }

    private void addMarkerAnnotation() {
        instrumentator.addMarkerAnnotation(this, USE_PROXY_INSTRUMENTED_ANNOTATION);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
        sourceFileName = source;
    }

    @Override
    public void visitEnd() {
        if(detector.hasProxiedMethods()) {
            if (!hasStaticInitializer) {
                generateStaticInitializer();
            }

            if (!hasReadObject) {
                generateReadObject();
            }

            generateInitProxyFactoriesStaticMethod();

            generateInitProxyFactoriesMethod();

            for (ProxiedMethodContext context : detector.getProxiedMethods().values()) {
                visitField(context.isStatic() ? ACC_PRIVATE | ACC_STATIC : ACC_PRIVATE | ACC_TRANSIENT, PROXY_FACTORY_FIELD_PREFIX + context.getId(), PROXY_FACTORY_TYPE.getDescriptor(), null, null);
            }
        }
        super.visitEnd();
    }

    protected abstract void generateInitProxyFactoriesStaticMethod();

    protected abstract void generateInitProxyFactoriesMethod();

    protected abstract void generateReadObject();

    protected abstract MethodVisitor createProxyFactoryInitializer(int access, String name, String desc, MethodVisitor oldVisitor);

    private void generateStaticInitializer() {
        MethodVisitor visitor = super.visitMethod(STATIC_INITIALIZER_ACCESS, STATIC_INITIALIZER_NAME, "()V", null, null);
        visitor.visitCode();
        visitor.visitMethodInsn(INVOKESTATIC, thisType.getInternalName(), RuntimeTypes.INIT_PROXY_FACTORIES_STATIC_METHOD.getName(), RuntimeTypes.INIT_PROXY_FACTORIES_STATIC_METHOD.getDescriptor());
        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(final int access, String name, final String desc, final String signature, final String[] exceptions) {
        MethodVisitor oldVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (!detector.hasProxiedMethods()) {
            return oldVisitor;
        }

        if (!Modifier.isStatic(access) && name.equals(CONSTRUCTOR_NAME)) {
            return createProxyFactoryInitializer(access, name, desc, oldVisitor);
        }

        if (READ_OBJECT_METHOD.getName().equals(name) && READ_OBJECT_METHOD.getDescriptor().equals(desc)) {
            if (Modifier.isStatic(access)) {
                throw new IllegalCachedClass("Method readObject(ObjectInputStream) should not static", sourceFileName);
            }
            if (!Modifier.isPrivate(access)) {
                throw new IllegalCachedClass("Method readObject(ObjectInputStream) should be private", sourceFileName);
            }
            hasReadObject = true;
            return createProxyFactoryInitializer(access, name, desc, oldVisitor);
        }
        if (Modifier.isStatic(access) && name.equals(STATIC_INITIALIZER_NAME)) {
            hasStaticInitializer = true;
            return new ProxyFactoryStaticInitializer(oldVisitor, access, name, desc);
        }
        final Method method = new Method(name, desc);
        final ProxiedMethodContext context = detector.getProxiedMethodContext(method);
        if (context == null) {
            return oldVisitor;
        }
        return new ProxyedGeneratorAdapter(access, method, oldVisitor, context, desc, signature, exceptions);
    }

    private class ProxyFactoryStaticInitializer extends AdviceAdapter {
        public ProxyFactoryStaticInitializer(MethodVisitor oldVisitor, int access, String name, String desc) {
            super(Opcodes.ASM4, oldVisitor, access, name, desc);
        }

        @Override
        protected void onMethodEnter() {
            invokeStatic(thisType, RuntimeTypes.INIT_PROXY_FACTORIES_STATIC_METHOD);
        }
    }

    private class ProxyedGeneratorAdapter extends MxGeneratorAdapter {
        private final int access;
        private final Method method;
        private final ProxiedMethodContext context;
        private final String desc;
        private final String signature;
        private final String[] exceptions;

        public ProxyedGeneratorAdapter(int access, Method method, MethodVisitor oldVisitor, ProxiedMethodContext context, String desc, String signature, String[] exceptions) {
            super(access, method, oldVisitor, thisType);
            this.access = access;
            this.method = method;
            this.context = context;
            this.desc = desc;
            this.signature = signature;
            this.exceptions = exceptions;
        }

        @Override
        public void visitCode() {
            super.visitCode();

            boolean isStatic = Modifier.isStatic(access);

            if (isStatic) {
                getStatic(thisType, PROXY_FACTORY_FIELD_PREFIX + context.getId(), PROXY_FACTORY_TYPE);
            } else {
                loadThis();
                getField(thisType, PROXY_FACTORY_FIELD_PREFIX + context.getId(), PROXY_FACTORY_TYPE);
            }
            dup();
            Label noProxy = new Label();
            ifNull(noProxy);
            push(method.getReturnType());
            Type resolvableType = getObjectType(ResolvableGenerator.getResolvableName(context.getId(), thisType));
            newInstance(resolvableType);
            dup();
            if (!isStatic) {
                loadThis();
            }
            Type[] arguments = method.getArgumentTypes();
            for (int i = 0; i< arguments.length; i++) {
                loadArg(i);
            }
            Type[] ctorArguments = isStatic ? arguments : insertFirst(arguments, thisType);
            invokeConstructor(resolvableType, new Method(CONSTRUCTOR_NAME, VOID_TYPE, ctorArguments));
            invokeInterface(PROXY_FACTORY_TYPE, PROXY_METHOD);
            checkCast(Type.getReturnType(desc));
            returnValue();

            mark(noProxy);
            pop();
            generateNonProxiedDelegate(isStatic, arguments);

            endMethod();
            mv = cv.visitMethod(isStatic ? ACC_STATIC | ACC_SYNTHETIC : ACC_SYNTHETIC, ResolvableGenerator.getNonProxiedMethodName(method), desc, signature, exceptions);
            mv.visitCode();
        }

        private void generateNonProxiedDelegate(boolean aStatic, Type[] arguments) {
            if (!aStatic) {
                loadThis();
            }
            for (int i = 0; i < arguments.length; i++) {
                loadArg(i);
            }
            String nonProxiedMethodName = ResolvableGenerator.getNonProxiedMethodName(method);
            visitMethodInsn(aStatic ? INVOKESTATIC : INVOKEVIRTUAL, thisType.getInternalName(), nonProxiedMethodName, method.getDescriptor());
            returnValue();
        }
    }

    public Type getThisType() {
        return thisType;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }
}
