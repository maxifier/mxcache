package com.maxifier.mxcache.size;

import gnu.trove.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.maxifier.mxcache.util.FormatHelper.formatSize;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 02.02.2009
 * Time: 12:37:11
 */
public final class ObjectSizeHelper {
    public static final int OBJREF_SIZE = 4;
    public static final int LONG_FIELD_SIZE = 8;
    public static final int INT_FIELD_SIZE = 4;
    public static final int SHORT_FIELD_SIZE = 2;
    public static final int CHAR_FIELD_SIZE = 2;
    public static final int BYTE_FIELD_SIZE = 1;
    public static final int BOOLEAN_FIELD_SIZE = 1;
    public static final int DOUBLE_FIELD_SIZE = 8;
    public static final int FLOAT_FIELD_SIZE = 4;

    public static final int PADDING_4 = 4;

    public static final int OBJECT_HEADER_SIZE = 8;
    public static final int ARRAY_HEADER_SIZE = OBJECT_HEADER_SIZE + INT_FIELD_SIZE;
    public static final int LONG_ARRAY_HEADER_SIZE = OBJECT_HEADER_SIZE + INT_FIELD_SIZE + PADDING_4;

    private ObjectSizeHelper() {
    }

    static final TObjectIntHashMap<Class> PRIMITIVE_SIZES = new TObjectIntHashMap<Class>() {
        {
            put(boolean.class, BOOLEAN_FIELD_SIZE);
            put(byte.class, BYTE_FIELD_SIZE);
            put(char.class, CHAR_FIELD_SIZE);
            put(short.class, SHORT_FIELD_SIZE);
            put(int.class, INT_FIELD_SIZE);
            put(long.class, LONG_FIELD_SIZE);
            put(float.class, FLOAT_FIELD_SIZE);
            put(double.class, DOUBLE_FIELD_SIZE);
        }
    };

    public static final SizeCalculator<Object> STANDARD_SIZE_CALCULATORS;

    private static final SizeCalculator<Object> EMPTY_CALCULATOR = new SizeCalculator<Object>() {
        @Override
        public int getApproximateSize(@NotNull Object o, @NotNull SizeIterator iterator) {
            return 0;
        }
    };

    //------------------------------------------------------------------------------------------------------------------

    static {
        ComplexSizeCalculatorBuilder<Object> builder = new ComplexSizeCalculatorBuilder<Object>();
        builder.registerSizeCalculator(boolean[].class, new SizeCalculator<boolean[]>() {
            @Override
            public int getApproximateSize(@NotNull boolean[] o, @NotNull SizeIterator iterator) {
                //noinspection PointlessArithmeticExpression
                return ARRAY_HEADER_SIZE + alignTo4(o.length * BOOLEAN_FIELD_SIZE);
            }
        });
        builder.registerSizeCalculator(byte[].class, new SizeCalculator<byte[]>() {
            @Override
            public int getApproximateSize(@NotNull byte[] o, @NotNull SizeIterator iterator) {
                //noinspection PointlessArithmeticExpression
                return ARRAY_HEADER_SIZE + alignTo4(o.length * BYTE_FIELD_SIZE);
            }
        });
        builder.registerSizeCalculator(char[].class, new SizeCalculator<char[]>() {
            @Override
            public int getApproximateSize(@NotNull char[] o, @NotNull SizeIterator iterator) {
                return ARRAY_HEADER_SIZE + alignTo4(o.length * CHAR_FIELD_SIZE);
            }
        });
        builder.registerSizeCalculator(short[].class, new SizeCalculator<short[]>() {
            @Override
            public int getApproximateSize(@NotNull short[] o, @NotNull SizeIterator iterator) {
                return ARRAY_HEADER_SIZE + alignTo4(o.length * SHORT_FIELD_SIZE);
            }
        });
        builder.registerSizeCalculator(int[].class, new SizeCalculator<int[]>() {
            @Override
            public int getApproximateSize(@NotNull int[] o, @NotNull SizeIterator iterator) {
                return ARRAY_HEADER_SIZE + o.length * INT_FIELD_SIZE;
            }
        });
        builder.registerSizeCalculator(long[].class, new SizeCalculator<long[]>() {
            @Override
            public int getApproximateSize(@NotNull long[] o, @NotNull SizeIterator iterator) {
                return LONG_ARRAY_HEADER_SIZE + o.length * LONG_FIELD_SIZE;
            }
        });
        builder.registerSizeCalculator(float[].class, new SizeCalculator<float[]>() {
            @Override
            public int getApproximateSize(@NotNull float[] o, @NotNull SizeIterator iterator) {
                return ARRAY_HEADER_SIZE + o.length * FLOAT_FIELD_SIZE;
            }
        });
        builder.registerSizeCalculator(double[].class, new SizeCalculator<double[]>() {
            @Override
            public int getApproximateSize(@NotNull double[] o, @NotNull SizeIterator iterator) {
                return LONG_ARRAY_HEADER_SIZE + o.length * DOUBLE_FIELD_SIZE;
            }
        });
        builder.registerSizeCalculator(Object[].class, new SizeCalculator<Object[]>() {
            @Override
            public int getApproximateSize(@NotNull Object[] o, @NotNull SizeIterator iterator) {
                for (Object r : o) {
                    iterator.pass("[Array element] (" + (r == null ? "null" : r.getClass().getSimpleName()) + ")", r);
                }
                return ARRAY_HEADER_SIZE + o.length * OBJREF_SIZE;
            }
        });
        builder.registerSizeCalculator(Date.class, new SizeCalculator<Date>() {
            @Override
            public int getApproximateSize(@NotNull Date o, @NotNull SizeIterator iterator) {
                return OBJECT_HEADER_SIZE + OBJREF_SIZE + LONG_FIELD_SIZE;
            }
        });

        builder.registerImplicitSizable(Arrays.asList("A").getClass());
        builder.registerImplicitSizable(Collections.singletonList("A").getClass());
        builder.registerImplicitSizable(Collections.singleton("A").getClass());
        builder.registerImplicitSizable(Collections.emptyList().getClass());
        builder.registerImplicitSizable(Collections.emptyMap().getClass());
        builder.registerImplicitSizable(Collections.emptySet().getClass());
        builder.registerImplicitSizable(Collections.unmodifiableCollection(Collections.emptyList()).getClass());
        builder.registerImplicitSizable(EnumSet.class);
        builder.registerImplicitSizable(Boolean.class);
        builder.registerImplicitSizable(Character.class);
        builder.registerImplicitSizable(Number.class);

        builder.registerImplicitSizable(THash.class);
        builder.registerImplicitSizable(Map.Entry.class);
        builder.registerImplicitSizable(HashMap.class);
        builder.registerImplicitSizable(HashSet.class);
        builder.registerImplicitSizable(ArrayList.class);
        builder.registerImplicitSizable(String.class);

        builder.registerSizeCalculator(Enum.class, ObjectSizeHelper.<Enum>emptyCalculator());
        builder.registerSizeCalculator(Class.class, ObjectSizeHelper.<Class>emptyCalculator());

        STANDARD_SIZE_CALCULATORS = builder.build();
    }

    public static int alignTo4(int length) {
        if ((length & 3) != 0) {
            return (length & ~3) + 4;
        }
        return length;
    }

    //------------------------------------------------------------------------------------------------------------------

    @SuppressWarnings({"unchecked"})
    public static <T> SizeCalculator<T> emptyCalculator() {
        return (SizeCalculator<T>) EMPTY_CALCULATOR;
    }

    public static ObjectSize getApproximateSize(SizeCalculator<Object> root, Object... sizable) {
        SizeMeasurer measurer = new SizeMeasurer(root);
        for (Object o : sizable) {
            measurer.pass("root", o);
        }
        return measurer.getSize();
    }

    public static ObjectSize getApproximateSize(Object... sizable) {
        return getApproximateSize(STANDARD_SIZE_CALCULATORS, sizable);
    }

    //------------------------------------------------------------------------------------------------------------------

    private static class SizeStat {
        private int shallowSize;
        private int instances;
        private int deepSize;

        void add(int shallowSize, int deepSize) {
            instances++;
            this.shallowSize += shallowSize;
            this.deepSize += deepSize;
        }

        public int getShallowSize() {
            return shallowSize;
        }

        public int getInstances() {
            return instances;
        }

        public int getDeepSize() {
            return deepSize;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) for %d (%s (%s) per instance)", formatSize(shallowSize), formatSize(deepSize), instances, formatSize(shallowSize / instances), formatSize(deepSize / instances));
        }
    }

    public static class ObjectSize {
        private final int size;
        private final Map<Class, SizeStat> byClass;
        private final Map<Object, SizeStat> byKey;
        private final TObjectIntHashMap<Object> passed;

        public ObjectSize(int size, Map<Class, SizeStat> byClass, TObjectIntHashMap<Object> passed, Map<Object, SizeStat> byKey) {
            this.size = size;
            this.byClass = byClass;
            this.passed = passed;
            this.byKey = byKey;
        }

        public int getSize() {
            return size;
        }

        public Map<Class, SizeStat> getByClass() {
            return byClass;
        }

        public TObjectIntHashMap<Object> getPassed() {
            return passed;
        }

        public Map<Object, SizeStat> getByKey() {
            return byKey;
        }

        public String toString() {
            return formatSize(size);
        }
    }

    private static class SizeMeasurer implements SizeIterator {
        private int size;
        private final TObjectIntHashMap<Object> passed = new TObjectIntHashMap<Object>(new TObjectIdentityHashingStrategy<Object>());
        private final Map<Class, SizeStat> byClass = new THashMap<Class, SizeStat>();
        private final Map<Object, SizeStat> byKey = new THashMap<Object, SizeStat>();
        private final SizeCalculator<Object> calculator;

        public SizeMeasurer(SizeCalculator<Object> calculator) {
            this.calculator = calculator;
        }

        private abstract static class X extends TObjectHash {
            static final Object FREE_VALUE = TObjectHash.FREE;
            static final Object REMOVED_VALUE = TObjectHash.REMOVED;
        }

        @Override
        public <T> void pass(Object key, T o) {
            if (o == X.FREE_VALUE || o == X.REMOVED_VALUE) {
                o = null;
            }
            if (o != null && !passed.containsKey(o)) {
                passed.adjustOrPutValue(o, 1, 1);
                int oldSize = this.size;

                Class c = o.getClass();
                SizeStat classStat = byClass.get(c);
                if (classStat == null) {
                    classStat = new SizeStat();
                    byClass.put(c, classStat);
                }
                int oldClassSize = classStat.getDeepSize();

                SizeStat keyStat = byKey.get(key);
                if (keyStat == null) {
                    keyStat = new SizeStat();
                    byKey.put(key, keyStat);
                }
                int oldKeySize = keyStat.getDeepSize();

                int size = calculator.getApproximateSize(o, this);

                this.size += size;
                int deepSize = this.size - oldSize;

                classStat.add(size, deepSize - (classStat.getDeepSize() - oldClassSize));
                keyStat.add(size, deepSize - (keyStat.getDeepSize() - oldKeySize));
            }
        }

        public ObjectSize getSize() {
            return new ObjectSize(size, byClass, passed, byKey);
        }
    }
}
