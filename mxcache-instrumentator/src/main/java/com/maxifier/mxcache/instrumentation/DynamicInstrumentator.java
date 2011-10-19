package com.maxifier.mxcache.instrumentation;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import com.maxifier.mxcache.instrumentation.current.InstrumentatorImpl;
import com.maxifier.mxcache.util.CodegenHelper;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 25.01.2010
 * Time: 14:20:31
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public class DynamicInstrumentator implements ClassFileTransformer {
    private final InstrumentatorImpl instrumentator;

    public DynamicInstrumentator(InstrumentatorImpl instrumentator) {
        this.instrumentator = instrumentator;
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new DynamicInstrumentator(InstrumentatorImpl.CURRENT_INSTANCE), true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] bytecode) throws IllegalClassFormatException {
        ClassInstrumentationResult result = instrumentator.instrument(bytecode);
        if (result == null) {
            return null;
        }
        try {
            for (ClassDefinition classDefinition : result.getAdditionalClasses()) {
                CodegenHelper.loadClass(loader, classDefinition.getBytecode());
            }
            return result.getInstrumentedBytecode();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[] {};
        }
    }
}
