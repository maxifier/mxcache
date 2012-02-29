package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.ClassAdapter;
import com.maxifier.mxcache.asm.ClassVisitor;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.AdviceAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.util.CodegenHelper;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import java.lang.reflect.Modifier;

import static com.maxifier.mxcache.asm.Opcodes.INVOKESTATIC;
import static com.maxifier.mxcache.asm.Opcodes.RETURN;
import static com.maxifier.mxcache.util.CodegenHelper.STATIC_INITIALIZER_ACCESS;
import static com.maxifier.mxcache.util.CodegenHelper.STATIC_INITIALIZER_NAME;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 29.02.12
 * Time: 15:00
 */
public class AddInstanceInitializer extends ClassAdapter {
    private final Method method;

    private Type thisType;

    public AddInstanceInitializer(ClassVisitor cv, Method method) {
        super(cv);
        this.method = method;
    }

    @Override
    public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        thisType = Type.getObjectType(className);
        super.visit(version, access, className, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, String sign, String[] exceptions) {
        if (!Modifier.isStatic(access) && name.equals(CodegenHelper.CONSTRUCTOR_NAME)) {
            return new AdviceAdapter(super.visitMethod(access, name, desc, sign, exceptions), access, name, desc) {
                @Override
                protected void onMethodEnter() {
                    loadThis();
                    invokeVirtual(thisType, method);
                }
            };
        }
        return super.visitMethod(access, name, desc, sign, exceptions);
    }
}
