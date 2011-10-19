package com.maxifier.mxcache.size;

import gnu.trove.THashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 11.02.2009
 * Time: 16:23:44
 */
public class ComplexSizeCalculatorBuilder<T> {
    private final Map<Class, SizeCalculator> sizeCalculators = new THashMap<Class, SizeCalculator>();
    private final List<Class> explicitSizable = new ArrayList<Class>();
    private final SizeCalculator<Object> root;

    public ComplexSizeCalculatorBuilder() {
        root = null;
    }

    public ComplexSizeCalculatorBuilder(SizeCalculator<Object> root) {
        this.root = root;
    }

    public synchronized void registerImplicitSizable(Class<?> cls) {
        explicitSizable.add(cls);
    }

    public synchronized <R extends T> void registerSizeCalculator(Class<R> cls, SizeCalculator<R> calculator) {
        final SizeCalculator c = sizeCalculators.get(cls);
        if (c != null) {
            throw new RuntimeException("Cannot reassign calculator");
        }
        sizeCalculators.put(cls, calculator);
    }

    public synchronized ComplexSizeCalculator<T> build() {
        return new ComplexSizeCalculator<T>(root, sizeCalculators, explicitSizable);
    }
}
