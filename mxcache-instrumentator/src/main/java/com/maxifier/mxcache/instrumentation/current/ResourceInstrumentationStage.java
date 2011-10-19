package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.instrumentation.ClassDefinition;
import com.maxifier.mxcache.instrumentation.InstrumentationStage;
import com.maxifier.mxcache.asm.*;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.*;
import static com.maxifier.mxcache.util.CodegenHelper.STATIC_INITIALIZER_NAME;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 12.04.2010
 * Time: 12:38:31
 */
class ResourceInstrumentationStage extends ClassAdapter implements InstrumentationStage {
    private static final String RESOURCE_INITIALIZER_METHOD_NAME = "$initResources$";

    private String thisClass;
    private boolean hasStaticInitializer;

    private final InstrumentatorImpl instrumentator;

    private final ResourceDetector detector;

    public ResourceInstrumentationStage(InstrumentatorImpl instrumentator, ClassVisitor cv, ClassVisitor nextDetector) {
        super(cv);
        this.detector = new ResourceDetector(nextDetector);
        this.instrumentator = instrumentator;
    }

    @Override
    public ClassVisitor getDetector() {
        return detector;
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        if (detector.hasResourceAccessors()) {
            addMarkerAnnotation();
        }
        thisClass = name;
    }

    private void addMarkerAnnotation() {
        instrumentator.addMarkerAnnotation(this, RESOURCE_INSTRUMENTED_ANNOTATION);
    }

    @Override
    public void visitEnd() {
        String method = hasStaticInitializer ? RESOURCE_INITIALIZER_METHOD_NAME : STATIC_INITIALIZER_NAME;
        MethodVisitor initializer = super.visitMethod(ACC_STATIC | ACC_PRIVATE | ACC_SYNTHETIC, method, "()V", null, null);
        initializer.visitCode();
        for (Map.Entry<String, String> e : detector.getResourceToFieldMapping().entrySet()) {
            String field = e.getValue();
            visitField(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, field, RuntimeTypes.MX_RESOURCE_TYPE.getDescriptor(), null, null);
            initializer.visitLdcInsn(e.getKey());
            initializer.visitMethodInsn(INVOKESTATIC, RuntimeTypes.MX_RESOURCE_MANAGER_TYPE.getInternalName(), RuntimeTypes.GET_RESOURCE_METHOD.getName(), RuntimeTypes.GET_RESOURCE_METHOD.getDescriptor());
            initializer.visitFieldInsn(PUTSTATIC, thisClass, field, RuntimeTypes.MX_RESOURCE_TYPE.getDescriptor());
        }
        initializer.visitInsn(RETURN);
        initializer.visitMaxs(0, 0);
        initializer.visitEnd();
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (Modifier.isStatic(access) && name.equals(STATIC_INITIALIZER_NAME)) {
            hasStaticInitializer = true;
            return new MethodAdapter(visitor) {
                @Override
                public void visitCode() {
                    super.visitCode();
                    visitMethodInsn(INVOKESTATIC, thisClass, RESOURCE_INITIALIZER_METHOD_NAME, "()V");
                }
            };
        }

        Method method = new Method(name, desc);
        ResourceMethodContext context = detector.getResourceAccessorContext(method);
        if (context == null) {
            return visitor;
        }
        return new ResourceMethodVisitor(visitor, access, name, desc, Type.getObjectType(thisClass), context);
    }



    @Override
    public boolean isClassChanged() {
        return detector.isClassChanged();
    }

    @Override
    public List<ClassDefinition> getAdditionalClasses() {
        return Collections.emptyList();
    }
}
