package com.maxifier.mxcache.size;

import javax.annotation.Nonnull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 11.02.2009
 * Time: 16:23:48
 */
public class ComplexSizeCalculator<T> implements SizeCalculator<T> {
    private static final SizeCalculator<Object> OBJECT_CALCULATOR = new SizeCalculator<Object>() {
        @Override
        public int getApproximateSize(@Nonnull Object o, @Nonnull SizeIterator iterator) {
            return ObjectSizeHelper.OBJECT_HEADER_SIZE;
        }
    };
    private final SizeCalculator<Object> root;
    private final Map<Class, SizeCalculator> sizeCalculators;
    private final List<Class> explicitSizable;

    private class SizableScheme implements SizeCalculator {
        private final List<Field> fields;
        private final SizeCalculator<Object> parentCalculator;
        private final int instanceSize;

        public SizableScheme(Class c) {
            fields = new ArrayList<Field>();
            int instanceSize = 0;

            int byteFields = 0;
            int shortFields = 0;
            for (final Field field : c.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    Class<?> type = field.getType();
                    if (type == boolean.class || type == byte.class) {
                        byteFields++;
                    } else if (type == char.class || type == short.class) {
                        shortFields++;
                    } else if (type == int.class || type == float.class) {
                        instanceSize += ObjectSizeHelper.INT_FIELD_SIZE;
                    } else if (type == long.class || type == double.class) {
                        instanceSize += ObjectSizeHelper.LONG_FIELD_SIZE;
                    } else {
                        assert !type.isPrimitive();
                        instanceSize += ObjectSizeHelper.OBJREF_SIZE;
                        if (field.getAnnotation(SizeTransient.class) == null) {
                            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                                @Override
                                public Object run() {
                                    field.setAccessible(true);
                                    return null;
                                }
                            });
                            fields.add(field);
                        }
                    }
                }
            }
            instanceSize += ObjectSizeHelper.alignTo4(byteFields * ObjectSizeHelper.BYTE_FIELD_SIZE);
            instanceSize += ObjectSizeHelper.alignTo4(shortFields * ObjectSizeHelper.SHORT_FIELD_SIZE);

            this.instanceSize = instanceSize;
            parentCalculator = findForClass(c.getSuperclass());
        }

        @Override
        public int getApproximateSize(@Nonnull Object o, @Nonnull SizeIterator iterator) {
            for (Field f : fields) {
                try {
                    Object fv = f.get(o);
                    iterator.pass(f + "(" + (fv == null ? "null" : fv.getClass().getSimpleName()) + ")", fv);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (RuntimeException e) {
                    throw new RuntimeException(f.toString(), e);
                }
            }
            if (parentCalculator == null) {
                return instanceSize;
            }
            return instanceSize + parentCalculator.getApproximateSize(o, iterator);
        }
    }

    ComplexSizeCalculator(SizeCalculator<Object> root, Map<Class, SizeCalculator> sizeCalculators, List<Class> explicitSizable) {
        this.root = root;
        this.sizeCalculators = sizeCalculators;
        this.explicitSizable = explicitSizable;
    }

    private synchronized SizeCalculator<Object> findForClass(Class<?> c) {
        if (c == Object.class) {
            return OBJECT_CALCULATOR;
        }
        if (c.getAnnotation(EmptySize.class) != null) {
            return ObjectSizeHelper.emptyCalculator();
        }
        SizeCalculator calculator = sizeCalculators.get(c);
        if (calculator == null) {
            for (Class cls : explicitSizable) {
                if (cls.isAssignableFrom(c)) {
                    calculator = new SizableScheme(c);
                    break;
                }
            }

            if (calculator == null) {
                if (c.getAnnotation(Sizable.class) == null) {
                    Class x = null;
                    for (Map.Entry<Class, SizeCalculator> e : sizeCalculators.entrySet()) {
                        Class z = e.getKey();
                        if (z.isAssignableFrom(c)) {
                            if (x == null || x.isAssignableFrom(z)) {
                                x = z;
                                calculator = e.getValue();
                            } else if (!z.isAssignableFrom(x)) {
                                throw new RuntimeException(c + " can be sized by different sizers: of " + x + " and of " + z);
                            }
                        }
                    }
                } else {
                    calculator = new SizableScheme(c);
                }
            }
            sizeCalculators.put(c, calculator);
        }
        //noinspection unchecked
        return calculator;
    }

    @Override
    public int getApproximateSize(@Nonnull T o, @Nonnull SizeIterator iterator) {
        SizeCalculator<Object> calculator = findForClass(o.getClass());
        if (calculator == null) {
            if (root == null) {
                throw new UnsupportedOperationException("No size calculator for " + o.getClass() + ": " + o);
            }
            return root.getApproximateSize(o, iterator);
        }
        return calculator.getApproximateSize(o, iterator);
    }
}
