/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.instrumentation.IllegalCachedClass;
import com.maxifier.mxcache.asm.*;
import gnu.trove.map.hash.THashMap;

import java.lang.reflect.Modifier;
import java.util.Map;

import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.USE_PROXY_INSTRUMENTED_ANNOTATION;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class UseProxyDetector extends ClassVisitor {
    private final Map<Method, ProxiedMethodContext> proxiedMethods = new THashMap<Method, ProxiedMethodContext>();

    private int classAccess;

    private int id;

    private Type thisClass;

    private String sourceFileName;

    private boolean alreadyInstrumented;

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        classAccess = access;
        thisClass = Type.getObjectType(name);
    }

    public UseProxyDetector(ClassVisitor nextDetector) {
        super(Opcodes.ASM5, nextDetector);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(USE_PROXY_INSTRUMENTED_ANNOTATION.getDescriptor())) {
            alreadyInstrumented = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor oldVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (alreadyInstrumented) {
            return oldVisitor;
        }
        return new ProxyMethodDetector(oldVisitor, new Method(name, desc), access);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
        sourceFileName = source;
    }

    private class ProxyMethodDetector extends MethodVisitor {
        private final Method method;

        private final ProxiedMethodContext context;

        private final int methodAccess;

        private boolean useProxy;

        private boolean cached;

        public ProxyMethodDetector(MethodVisitor oldVisitor, Method method, int methodAccess) {
            super(Opcodes.ASM5, oldVisitor);
            this.method = method;
            this.methodAccess = methodAccess;
            context = new ProxiedMethodContext(id++, Modifier.isStatic(this.methodAccess), method);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationVisitor v = super.visitAnnotation(desc, visible);
            if (desc.equals(InstrumentatorImpl.CACHED_DESCRIPTOR)) {
                cached = true;
                proxiedMethods.remove(method);
                useProxy = false;
            } else if (desc.equals(InstrumentatorImpl.USE_PROXY_DESCRIPTOR) && !cached) {
                proxiedMethods.put(method, context);
                useProxy = true;
            }
            return v;
        }

        @Override
        public void visitEnd() {
            if (useProxy) {
                if (Modifier.isInterface(classAccess)) {
                    throw new IllegalCachedClass("Interface should not have proxied methods: " + thisClass.getClassName() + "." + method, sourceFileName);
                }
                if (Modifier.isAbstract(methodAccess)) {
                    throw new IllegalCachedClass("Proxied method should not be abstract: " + thisClass.getClassName() + "." + method, sourceFileName);
                }
                if (Modifier.isNative(methodAccess)) {
                    throw new IllegalCachedClass("Proxied method should not be native: " + thisClass.getClassName() + "." + method, sourceFileName);
                }
                if (!isReferenceType(method.getReturnType())) {
                    throw new IllegalCachedClass("Proxied method should return object: " + thisClass.getClassName() + "." + method, sourceFileName);
                }
            }
        }
    }

    public ProxiedMethodContext getProxiedMethodContext(Method method) {
        return proxiedMethods.get(method);
    }

    public Map<Method, ProxiedMethodContext> getProxiedMethods() {
        return proxiedMethods;
    }

    public boolean hasProxiedMethods() {
        return !proxiedMethods.isEmpty();
    }
}
