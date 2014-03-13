/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.tuple.TupleGenerator;

/**
 * TupleInitializerGenerator
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TupleInitializerGenerator extends Generator {
    private static final Type TUPLE_GENERATOR_TYPE = Type.getType(TupleGenerator.class);

    private static final Method GET_TUPLE_CLASS_METHOD = Method.getMethod("Class getTupleClass(Class[])");

    private final Type[] args;

    public TupleInitializerGenerator(Type[] args) {
        this.args = args;
    }

    @Override
    public void generate(MxGeneratorAdapter sim) {
        sim.push(args.length);
        sim.newArray(CodegenHelper.CLASS_TYPE);
        for (int i = 0; i < args.length; i++) {
            sim.dup();
            sim.push(i);
            sim.push(args[i]);
            sim.arrayStore(CodegenHelper.CLASS_TYPE);
        }
        sim.invokeStatic(TUPLE_GENERATOR_TYPE, GET_TUPLE_CLASS_METHOD);
        sim.pop();
    }
}
