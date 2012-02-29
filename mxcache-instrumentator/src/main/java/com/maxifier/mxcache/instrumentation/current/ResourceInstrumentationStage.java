package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.asm.commons.SerialVersionUIDAdder;
import com.maxifier.mxcache.instrumentation.ClassDefinition;
import com.maxifier.mxcache.instrumentation.InstrumentationStage;
import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.util.MxField;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.*;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 12.04.2010
 * Time: 12:38:31
 */
abstract class ResourceInstrumentationStage extends SerialVersionUIDAdder implements InstrumentationStage {
    private static final Method RESOURCE_STATIC_INITIALIZER_METHOD = Method.getMethod("void $initResourcesStatic$()");
    private static final Method RESOURCE_INITIALIZER_METHOD = Method.getMethod("void $initResources$()");

    private Type thisClass;

    private final InstrumentatorImpl instrumentator;

    private final ResourceDetector detector;

    public ResourceInstrumentationStage(InstrumentatorImpl instrumentator, ClassVisitor cv, ClassVisitor nextDetector) {
        super(new AddInstanceInitializer(new AddStaticInitializer(cv, RESOURCE_STATIC_INITIALIZER_METHOD), RESOURCE_INITIALIZER_METHOD));
        this.detector = createDetector(nextDetector);
        this.instrumentator = instrumentator;
    }

    protected abstract ResourceDetector createDetector(ClassVisitor nextDetector);

    @Override
    public ClassVisitor getDetector() {
        return detector;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        thisClass = Type.getObjectType(name);
        super.visit(version, access, name, signature, superName, interfaces);
        if (detector.hasResourceAccessors()) {
            addMarkerAnnotation();
        }
    }

    private void addMarkerAnnotation() {
        instrumentator.addMarkerAnnotation(this, RESOURCE_INSTRUMENTED_ANNOTATION);
    }

    @Override
    public void visitEnd() {
        generateStaticInitializer();
        generateInstanceInitializer();
        super.visitEnd();
    }

    private void generateStaticInitializer() {
        MxGeneratorAdapter initializer = new MxGeneratorAdapter(STATIC_INITIALIZER_ACCESS, RESOURCE_STATIC_INITIALIZER_METHOD, thisClass, this);
        initializer.start();
        for (Map.Entry<String, MxField> e : detector.getResourceToFieldMappingStatic().entrySet()) {
            MxField field = e.getValue();
            field.define(this);

            initializer.push(e.getKey());
            initializer.invokeStatic(RuntimeTypes.MX_RESOURCE_MANAGER_TYPE, RuntimeTypes.GET_RESOURCE_METHOD);
            initializer.put(field);
        }
        initializer.returnValue();
        initializer.endMethod();
    }

    private void generateInstanceInitializer() {
        MxGeneratorAdapter initializer = new MxGeneratorAdapter(ACC_PRIVATE | ACC_SYNTHETIC, RESOURCE_INITIALIZER_METHOD, thisClass, this);
        initializer.start();
        for (Map.Entry<String, MxField> e : detector.getResourceToFieldMapping().entrySet()) {
            MxField field = e.getValue();
            field.define(this);

            initializer.loadThis();
            initializer.get(field);
            Label skipInitialization = new Label();
            initializer.ifNonNull(skipInitialization);

            initializer.loadThis();
            initializer.dup();
            initializer.push(e.getKey());
            initializer.invokeStatic(RuntimeTypes.MX_RESOURCE_MANAGER_TYPE, RuntimeTypes.CREATE_RESOURCE_METHOD);
            initializer.put(field);

            initializer.mark(skipInitialization);
        }
        initializer.returnValue();
        initializer.endMethod();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        Method method = new Method(name, desc);
        ResourceMethodContext context = detector.getResourceAccessorContext(method);
        if (context == null) {
            return visitor;
        }
        return new ResourceMethodVisitor(visitor, access, name, desc, thisClass, context);
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
