package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.IllegalCachedClass;
import com.maxifier.mxcache.asm.commons.EmptyVisitor;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.util.CodegenHelper;
import gnu.trove.THashMap;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.*;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.CACHE_INSTRUMENTED_ANNOTATION;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 19.10.2010
* Time: 12:56:36
*/
class CachedDetector extends ClassAdapter {
    private final Map<Method, CachedMethodContext> cachedMethods = new THashMap<Method, CachedMethodContext>();

    private Type thisClass;

    private String sourceFileName;

    private int classAccess;

    private boolean alreadyInstrumented;

    public CachedDetector(ClassVisitor nextDetector) {
        super(nextDetector);
    }

    @Override
    public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        super.visit(version, access, className, signature, superName, interfaces);
        thisClass = Type.getObjectType(className);
        classAccess = access;
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
        sourceFileName = source;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(CACHE_INSTRUMENTED_ANNOTATION.getDescriptor())) {
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
        if (Modifier.isStatic(access) && name.equals(CodegenHelper.STATIC_INITIALIZER_NAME)) {
            return oldVisitor;
        }
        if (READ_OBJECT_METHOD.getName().equals(name) && READ_OBJECT_METHOD.getDescriptor().equals(desc)) {
            if (Modifier.isStatic(access)) {
                throw new IllegalCachedClass("Method readObject(ObjectInputStream) should not static", sourceFileName);
            }
            if (!Modifier.isPrivate(access)) {
                throw new IllegalCachedClass("Method readObject(ObjectInputStream) should be private", sourceFileName);
            }
            return oldVisitor;
        }
        if (!Modifier.isStatic(access) && name.equals(CodegenHelper.CONSTRUCTOR_NAME)) {
            return oldVisitor;
        }

        return new MethodDetector(oldVisitor, desc, new Method(name, desc), access);
    }

    private final class MethodDetector extends MethodAdapter {
        private final String desc;

        private final Method method;

        private final CachedMethodContext context = new CachedMethodContext();

        private final int methodAccess;

        MethodDetector(MethodVisitor mv, String desc, Method method, int methodAccess) {
            super(mv);
            this.desc = desc;
            this.method = method;
            this.methodAccess = methodAccess;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String annotationClassInnerName, boolean visible) {
            if (InstrumentatorImpl.CACHED_DESCRIPTOR.equals(annotationClassInnerName)) {
                return visitCachedAnnotation();
            }
            context.getTags().add("@" + Type.getType(annotationClassInnerName).getClassName());
            return super.visitAnnotation(annotationClassInnerName, visible);
        }

        private AnnotationVisitor visitCachedAnnotation() {
            if (Modifier.isInterface(classAccess)) {
                throw new IllegalCachedClass("Interface should not have @Cached methods: " + thisClass.getClassName() + "." + method, sourceFileName);
            }
            if (Modifier.isAbstract(methodAccess)) {
                throw new IllegalCachedClass("@Cached method should not be abstract: " + thisClass.getClassName() + "." + method, sourceFileName);
            }
            if (Modifier.isNative(methodAccess)) {
                throw new IllegalCachedClass("@Cached method should not be native: " + thisClass.getClassName() + "." + method, sourceFileName);
            }
            if (Type.getReturnType(desc) == Type.VOID_TYPE) {
                throw new IllegalCachedClass("@Cached method should not return void: " + thisClass.getClassName() + "." + method, sourceFileName);
            }
            cachedMethods.put(method, context);
            return new CachedAnnotationVisitor();
        }

        private class CachedAnnotationVisitor implements AnnotationVisitor {
            @Override
            public void visit(String name, Object value) {
                if (name.equals("tags") && value instanceof String[]) {
                    List<String> tags = context.getTags();
                    for (String tag : (String[])value) {
                        tags.add(tag);
                    }
                } else if (name.equals("name") && value instanceof String) {
                    if (!value.equals("")) {
                        context.setName((String) value);
                    }
                } else if (name.equals("group")) {
                    context.setGroup((String) value);
                } else if (name.equals("activity")) {
                    if (!"".equals(value)) {
                        throw new IllegalCachedClass("Activity is not supported currently", sourceFileName);
                    }
                }
            }

            @Override
            public void visitEnum(String name, String desc, String value) {
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return null;
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                if (!name.equals("tags")) {
                    return null;
                }
                return new EmptyVisitor() {
                    @Override
                    public void visit(String name, Object value) {
                        context.getTags().add((String) value);
                    }
                };
            }

            @Override
            public void visitEnd() {
            }
        }
    }

    public boolean hasCachedMethods() {
        return !cachedMethods.isEmpty();
    }

    public CachedMethodContext getCachedMethodContext(Method method) {
        return cachedMethods.get(method);
    }
}
