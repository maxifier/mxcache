package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.IllegalCachedClass;
import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.commons.Method;
import gnu.trove.THashMap;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.RESOURCE_INSTRUMENTED_ANNOTATION;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 11.03.11
 * Time: 10:56
 */
public class ResourceDetector extends ClassAdapter {
    private final Map<Method, ResourceMethodContext> resourceAccessors = new THashMap<Method, ResourceMethodContext>();
    private final Map<String, String> resourceToFieldMapping = new THashMap<String, String>();

    private int fieldID;

    private boolean isInterface;

    private String sourceFileName;

    private Type thisType;

    private boolean alreadyInstrumented;

    public ResourceDetector(ClassVisitor cv) {
        super(cv);
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
        return new MethodDetector(oldVisitor, access, new Method(name, desc));
    }

    private final class MethodDetector extends MethodAdapter {
        private final int access;
        private final Method method;

        private final ResourceMethodContext context = new ResourceMethodContext();

        private MethodDetector(MethodVisitor mv, int access, Method method) {
            super(mv);
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

        private class ResourceAnnotationVisitor implements AnnotationVisitor {
            private final Set<String> fields;
            private final Set<String> fieldsToCheck;
            private final List<String> orderedFields;

            public ResourceAnnotationVisitor(Set<String> fields, Set<String> fieldsToCheck, List<String> orderedFields) {
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

            private class ResourceValueAnnotationVisitor implements AnnotationVisitor {
                @Override
                public void visit(String name, Object value) {
                    String field = nextField((String) value);
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

    public Map<String, String> getResourceToFieldMapping() {
        return resourceToFieldMapping;
    }

    String nextField(String id) {
        String field = resourceToFieldMapping.get(id);
        if (field == null) {
            field = createField();
            resourceToFieldMapping.put(id, field);
        }
        return field;
    }

    private String createField() {
        return "$resource$" + fieldID++;
    }

    public boolean isClassChanged() {
        return fieldID > 0;
    }
}
