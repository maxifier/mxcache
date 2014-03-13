/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import com.maxifier.mxcache.util.CodegenHelper;

/**
 * DynamicInstrumentator
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class DynamicInstrumentator implements ClassFileTransformer {
    private final Instrumentator instrumentator;

    public DynamicInstrumentator(Instrumentator instrumentator) {
        this.instrumentator = instrumentator;
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new DynamicInstrumentator(InstrumentatorProvider.getPreferredVersion()), true);
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
