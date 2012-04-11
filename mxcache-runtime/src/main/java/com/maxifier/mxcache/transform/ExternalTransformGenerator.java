package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.InstanceProvider;
import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.util.ClassGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.maxifier.mxcache.asm.Type.getType;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 15.09.2010
* Time: 12:29:32
*/
public class ExternalTransformGenerator extends ScalarTransformGenerator {
    private static final String TRANSFORMATOR_FIELD = "transformator";

    private static final Type INSTANCE_PROVIDER_TYPE = getType(InstanceProvider.class);
    private static final Type CACHE_CONTEXT_TYPE = getType(CacheContext.class);

    private static final Method GET_INSTANCE_PROVIDER_METHOD = new Method("getInstanceProvider", INSTANCE_PROVIDER_TYPE, EMPTY_TYPES);
    private static final Method FOR_CLASS_METHOD = Method.getMethod("Object forClass(Class)");

    @NotNull
    private final InvocationType invocationType;
    @NotNull
    private final Type ownerType;
    @NotNull
    private final java.lang.reflect.Method method;
    @Nullable
    private final Type keyType;

    public ExternalTransformGenerator(@NotNull InvocationType invocationType, @NotNull Class owner, @NotNull java.lang.reflect.Method method) {
        this.invocationType = invocationType;
        this.ownerType = Type.getType(owner);
        this.method = method;

        Type[] argTypes = Type.getArgumentTypes(method);
        if (invocationType == InvocationType.KEY_INTERFACE || invocationType == InvocationType.KEY_VIRTUAL) {
            if (argTypes.length != 0) {
                throw new IllegalArgumentException("Transformator method of key should have no arguments");
            }
            keyType = null;
        } else {
            if (argTypes.length != 1) {
                throw new IllegalArgumentException("Transformator method should have exactly one argument");
            }
            keyType = argTypes[0];
        }
        if (method.getReturnType() == void.class) {
            throw new IllegalArgumentException("Transformator method shouldn't return void");
        }
    }

    @Override
    public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        if (invocationType.isKeyInvocation()) {
            method.checkCast(ownerType);
        } else if (invocationType.isRequiresKey() && isReferenceType(keyType)) {
            method.checkCast(keyType);
        }
        if (invocationType.isNeedsField()) {
            method.loadThis();
            method.getField(thisType, TRANSFORMATOR_FIELD + fieldIndex, ownerType);
            method.swap(keyType, OBJECT_TYPE);
        }
        method.visitMethodInsn(invocationType.getOpcode(), ownerType.getInternalName(), this.method.getName(), Type.getMethodDescriptor(this.method));
    }

    @Override
    public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void generateFields(Type thisType, int fieldIndex, ClassGenerator writer) {
        if (invocationType.isNeedsField()) {
            writer.defineField(Opcodes.ACC_PRIVATE, TRANSFORMATOR_FIELD + fieldIndex, ownerType);
        }
    }

    @Override
    public void generateAcquire(Type thisType, int fieldIndex, GeneratorAdapter ctor, int contextLocal) {
        if (invocationType.isNeedsField()) {
            ctor.loadThis();
            ctor.loadLocal(contextLocal);
            ctor.invokeInterface(CACHE_CONTEXT_TYPE, GET_INSTANCE_PROVIDER_METHOD);
            ctor.push(ownerType);
            ctor.invokeInterface(INSTANCE_PROVIDER_TYPE, FOR_CLASS_METHOD);
            ctor.checkCast(ownerType);
            ctor.putField(thisType, TRANSFORMATOR_FIELD + fieldIndex, ownerType);
        }
    }

    @Override
    public int getFieldCount() {
        return invocationType.isNeedsField() ? 1 : 0;
    }

    @Override
    public Class getTransformedType(Class in) {
        return method.getReturnType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExternalTransformGenerator that = (ExternalTransformGenerator) o;
        return method.equals(that.method) &&
                invocationType == that.invocationType &&
                (keyType == null ? that.keyType == null : keyType.equals(that.keyType)) &&
                ownerType.equals(that.ownerType);

    }

    @Override
    public int hashCode() {
        int result = invocationType.hashCode();
        result = 31 * result + ownerType.hashCode();
        result = 31 * result + method.hashCode();
        result = 31 * result + (keyType == null ? 0 : keyType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return invocationType + " " + ownerType.getClassName() + "." + method;
    }
}
