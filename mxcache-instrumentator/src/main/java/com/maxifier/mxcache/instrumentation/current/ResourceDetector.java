/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.instrumentation.IllegalCachedClass;
import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.util.MxField;
import gnu.trove.map.hash.THashMap;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.maxifier.mxcache.asm.Opcodes.ACC_PRIVATE;
import static com.maxifier.mxcache.asm.Opcodes.ACC_STATIC;
import static com.maxifier.mxcache.asm.Opcodes.ACC_SYNTHETIC;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.RESOURCE_INSTRUMENTED_ANNOTATION;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ResourceDetector extends ClassVisitor {
    private static final int STATIC_RESOURCE_FIELD_ACCESS = ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC;
    private static final int RESOURCE_FIELD_ACCESS = ACC_PRIVATE | ACC_SYNTHETIC;

    private final Map<Method, ResourceMethodContext> resourceAccessors = new THashMap<Method, ResourceMethodContext>();
    private final Map<String, MxField> resourceToFieldMapping = new THashMap<String, MxField>();
    private final Map<String, MxField> resourceToFieldMappingStatic = new THashMap<String, MxField>();

    private final boolean allowNonStatic;

    private int fieldID;

    private boolean isInterface;

    private String sourceFileName;

    private Type thisType;

    private boolean alreadyInstrumented;

    public ResourceDetector(ClassVisitor cv, boolean allowNonStatic) {
        super(Opcodes.ASM5, cv);
        this.allowNonStatic = allowNonStatic;
    }

    public boolean hasResourceAccessors() {
        return !resourceAccessors.isEmpty();
    }

    public ResourceMethodContext getResourceAccessorContext(Method method) {
        return resourceAccessors.get(method);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(RESOURCE_INSTRUMENTED_ANNOTATION.getDescriptor())) {
            alreadyInstrumented = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        isInterface = Modifier.isInterface(access);
        thisType = Type.getObjectType(name);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
        sourceFileName = source;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor oldVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (alreadyInstrumented) {
            return oldVisitor;
        }
        if (isBridge(access)) {
            // In JDK 8, parameter and method annotations are copied to synthetic bridge methods.
            // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6695379
            return oldVisitor;
        }
        return new MethodDetector(oldVisitor, access, new Method(name, desc));
    }

    private static boolean isBridge(int access) {
        return (access & Opcodes.ACC_BRIDGE) != 0;
    }

    private final class MethodDetector extends MethodVisitor {
        private final int access;
        private final Method method;

        private final ResourceMethodContext context = new ResourceMethodContext();

        private MethodDetector(MethodVisitor mv, int access, Method method) {
            super(Opcodes.ASM5, mv);
            this.access = access;
            this.method = method;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (InstrumentatorImpl.RESOURCE_READER_DESCRIPTOR.equals(desc)) {
                checkAccessor();
                return new ResourceAnnotationVisitor(context.getReadResourceFields(), context.getWriteResourceFields(), context.getReadResourceOrder());
            } else if (InstrumentatorImpl.RESOURCE_WRITER_DESCRIPTOR.equals(desc)) {
                checkAccessor();
                return new ResourceAnnotationVisitor(context.getWriteResourceFields(), context.getReadResourceFields(), context.getWriteResourceOrder());
            }
            return super.visitAnnotation(desc, visible);
        }

        private void checkAccessor() {
            if (isInterface) {
                throw new IllegalCachedClass("Interface should not have method that is declared to access resource: " + getMethodReference(), sourceFileName);
            }
            if (Modifier.isAbstract(access)) {
                throw new IllegalCachedClass("Method that is declared to access resource should not be abstract: " + getMethodReference(), sourceFileName);
            }
            if (Modifier.isNative(access)) {
                throw new IllegalCachedClass("Method that is declared to access resource should not be native: " + getMethodReference(), sourceFileName);
            }
        }

        private String getMethodReference() {
            return thisType.getClassName() + "." + method.getName();
        }

        private class ResourceAnnotationVisitor extends AnnotationVisitor {
            private final Set<MxField> fields;
            private final Set<MxField> fieldsToCheck;
            private final List<MxField> orderedFields;

            public ResourceAnnotationVisitor(Set<MxField> fields, Set<MxField> fieldsToCheck, List<MxField> orderedFields) {
                super(Opcodes.ASM5);
                this.fields = fields;
                this.fieldsToCheck = fieldsToCheck;
                this.orderedFields = orderedFields;
            }

            @Override
            public void visit(String name, Object value) {
                throw new IllegalCachedClass("Invalid field method " + name, sourceFileName);
            }

            @Override
            public void visitEnum(String name, String desc, String value) {
                throw new IllegalCachedClass("Invalid enum method " + name + desc, sourceFileName);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String desc) {
                throw new IllegalCachedClass("Invalid annotation method " + name + desc, sourceFileName);
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                if (name.equals("value")) {
                    return new ResourceValueAnnotationVisitor();
                }
                throw new IllegalCachedClass("Invalid array method " + name, sourceFileName);
            }

            @Override
            public void visitEnd() {
            }

            private class ResourceValueAnnotationVisitor extends AnnotationVisitor {
                private ResourceValueAnnotationVisitor() {
                    super(Opcodes.ASM5);
                }

                @Override
                public void visit(String name, Object value) {
                    String resourceName = (String) value;
                    boolean nonStatic = allowNonStatic && resourceName.startsWith("#");
                    if (nonStatic) {
                        if (Modifier.isStatic(access)) {
                            throw new IllegalCachedClass("Bound resource " + value + " is accessed from static method: " + getMethodReference(), sourceFileName);
                        }
                        resourceName = resourceName.substring(1);
                    }
                    MxField field = nextField(resourceName, !nonStatic);
                    if (fieldsToCheck.contains(field)) {
                        throw new IllegalCachedClass("Resource " + value + " should not be read and written in one method: " + getMethodReference(), sourceFileName);
                    }
                    if (fields.add(field)) {
                        orderedFields.add(field);
                        resourceAccessors.put(method, context);
                    } else {
                        throw new IllegalCachedClass("Resource " + value + " should not be used more than once: " + getMethodReference(), sourceFileName);
                    }
                }

                @Override
                public void visitEnum(String name, String desc, String value) {
                    throw new IllegalCachedClass("Invalid enum method " + name + desc, sourceFileName);
                }

                @Override
                public AnnotationVisitor visitAnnotation(String name, String desc) {
                    throw new IllegalCachedClass("Invalid annotation method " + name + desc, sourceFileName);
                }

                @Override
                public AnnotationVisitor visitArray(String name) {
                    throw new IllegalCachedClass("Invalid array method " + name, sourceFileName);
                }

                @Override
                public void visitEnd() {
                }
            }
        }
    }

    public Map<String, MxField> getResourceToFieldMapping() {
        return resourceToFieldMapping;
    }

    public Map<String, MxField> getResourceToFieldMappingStatic() {
        return resourceToFieldMappingStatic;
    }

    MxField nextField(String id, boolean isStatic) {
        Map<String, MxField> map = isStatic ? resourceToFieldMappingStatic : resourceToFieldMapping;
        MxField field = map.get(id);
        if (field == null) {
            field = new MxField(isStatic ? STATIC_RESOURCE_FIELD_ACCESS : RESOURCE_FIELD_ACCESS, thisType, createFieldName(), RuntimeTypes.MX_RESOURCE_TYPE);
            map.put(id, field);
        }
        return field;
    }

    private String createFieldName() {
        return "$resource$" + fieldID++;
    }

    public boolean isClassChanged() {
        return fieldID > 0;
    }
}
