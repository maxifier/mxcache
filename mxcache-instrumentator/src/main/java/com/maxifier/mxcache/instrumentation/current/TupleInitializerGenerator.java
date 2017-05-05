/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.tuple.TupleGenerator;
import com.maxifier.mxcache.util.CodegenHelper;
import com.maxifier.mxcache.util.Generator;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

/**
 * TupleInitializerGenerator
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class TupleInitializerGenerator extends Generator {
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
