/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.tuple;

import com.maxifier.mxcache.hashing.*;
import gnu.trove.strategy.HashingStrategy;
import gnu.trove.strategy.IdentityHashingStrategy;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

/**
 * TupleGeneratorUTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class TupleGeneratorUTest {
    private static Class[] array(Class... r) {
        return r;
    }

    @Test
    public void testPrimitives() throws Exception {
        check(array(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, float.class, double.class, double.class),
              true, (byte) 3, 'a', (short) 11, 441, 71L, 31f, Float.NaN, 44d, Double.NaN);
    }

    @Test
    public void testObject() throws Exception {
        check(array(String.class, Comparable.class, Object.class), "Test", 4, null);
    }

    @Test
    public void testArray() throws Exception {
        check(array(short[].class, int.class, Object[].class), new short[]{(short)14, (short)88}, 999, new String[]{"foo", "bar"});
    }

    @Test
    public void testMixed() throws Exception {
        check(array(String.class, int.class), "Test", 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidParam() throws Exception {
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[]{null}, String.class);
        Tuple tuple = f1.create("Test");
        tuple.get(1);
    }

    @Test
    public void testFactory() throws ClassNotFoundException {
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[]{new IdentityHashingStrategy<String>(), null}, String.class, int.class);
        Class<? extends Tuple> type = f1.getTupleClass();
        assertEquals(type.getClassLoader(), ClassLoader.getSystemClassLoader());
        assertEquals(Class.forName(type.getName()), type);
        f1.create("Test", 3);
    }

    @Test
    public void testCustomStrategy() throws Exception {
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[]{new IdentityHashingStrategy<String>(), null}, String.class, int.class);
        Tuple tuple1 = f1.create("Test", 3);
        // у int hashCode равен ему самому
        int sample = Arrays.hashCode(new int[] { System.identityHashCode("Test"), 3});
        assertTrue(tuple1.hashCode() == sample);

        HashingStrategy veryCustom = new HashingStrategy<String>() {
            @Override
            public int computeHashCode(String object) {
                throw new UnsupportedOperationException();
            }
            @Override
            public boolean equals(String o1, String o2) {
                return o1.equalsIgnoreCase(o2);
            }
        };
        TupleFactory f2 = TupleGenerator.createTupleFactory(new HashingStrategy[]{veryCustom, null}, String.class, int.class);
        Tuple tuple2 = f2.create("tEST", 3);

        assertTrue(tuple2.equals(tuple1)); // tuple2 ignores case
        assertFalse(tuple1.equals(tuple2)); // tuple1 uses identity hashing strategy
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIncompatibleStrategy() throws Exception {
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[]{new IdentityHashingStrategy()}, int.class);
        Tuple tuple1 = f1.create(3);
        Tuple tuple2 = f1.create(4);
        assert tuple1.equals(tuple2);
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testIncompatibleStrategyForBoolean() throws Exception {
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[]{new IdentityHashingStrategy()}, boolean.class);
        Tuple tuple1 = f1.create(true);
        Tuple tuple2 = f1.create(false);
        assert tuple1.equals(tuple2);
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testInvalidTypeInt() throws Exception {
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[]{null}, String.class);
        f1.create("Test").getInt(0);
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testInvalidTypeLong() throws Exception {
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[]{null}, String.class);
        f1.create("Test").getLong(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidConverts() throws Exception {
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[]{null}, long.class);
        f1.create(3L).getInt(0);
    }

    @Test
    public void testConverts() throws Exception {
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[4], byte.class, int.class, float.class, String.class);
        Tuple tuple = f1.create((byte)1, 3, 4f, "test");
        assertEquals(tuple.getByte(0), (byte)1);
        assertEquals(tuple.getChar(0), (char)1);
        assertEquals(tuple.getShort(0), (short)1);
        assertEquals(tuple.getInt(0), 1);
        assertEquals(tuple.getLong(0), 1L);

        assertEquals(tuple.getInt(1), 3);
        assertEquals(tuple.getLong(1), 3L);

        assertEquals(tuple.getFloat(2), 4f);
        assertEquals(tuple.getDouble(2), 4d);

        // cast necessary for JDK8 compilation
        assertEquals(tuple.<Object>get(0), (byte)1);
        assertEquals(tuple.<Object>get(1), 3);
        assertEquals(tuple.get(2), 4f);
        assertEquals(tuple.get(3), "test");
    }

    @Test
    public void testEquals() throws Exception {
        Class[] t = array(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, float.class, double.class, double.class, String.class, String.class);
        Object[] v1 = { true, (byte) 3, 'a', (short) 11, 441, 71L, 31f, Float.NaN, 44d, Double.NaN, "123", null};
        Object[] v2 = { false, (byte) 4, '7', (short) 12, 421, 33L, 4.5f, 0f, 0d, 3.1d, "321", "124365"};
        TupleFactory f1 = TupleGenerator.createTupleFactory(new HashingStrategy[12], t);

        Tuple t1a = f1.create(v1);
        Tuple t1b = f1.create(v1);

        assertTrue(t1a.equals(t1a));
        assertTrue(t1a.equals(t1b));
        assertTrue(t1b.equals(t1a));
        assertTrue(t1b.equals(t1b));

        int n = t.length;
        int N = 1 << n;
        Object[] s = new Object[n];
        for (int i = 1; i < N; i++) {
            for (int b = 0, m = i; b < n; b++, m >>= 1) {
                s[b] = (m & 1) == 0 ? v1[b] : v2[b];
            }
            Tuple t2 = f1.create(s);
            assertFalse(t1a.equals(t2));
            assertFalse(t2.equals(t1a));
            assertFalse(t1b.equals(t2));
            assertFalse(t2.equals(t1b));
            assertTrue(t2.equals(t2));
        }
    }

    private static Tuple check(Class[] types, Object... values) throws Exception {
        assert types.length == values.length;
        TupleFactory f = TupleGenerator.createTupleFactory(new HashingStrategy[types.length], types);
        Tuple tuple = f.create(values);
        assertEquals(tuple.size(), values.length);
        for (int i = 0; i < values.length; i++) {
            assertEquals(tuple.get(i), values[i]);
            Class type = types[i];
            if (type == boolean.class) {
                assertEquals(tuple.getBoolean(i), values[i]);
            } else if (type == byte.class) {
                assertEquals(tuple.getByte(i), values[i]);
            } else if (type == char.class) {
                assertEquals(tuple.getChar(i), values[i]);
            } else if (type == short.class) {
                assertEquals(tuple.getShort(i), values[i]);
            } else if (type == int.class) {
                assertEquals(tuple.getInt(i), values[i]);
            } else if (type == long.class) {
                assertEquals(tuple.getLong(i), values[i]);
            } else if (type == float.class) {
                assertEquals(tuple.getFloat(i), values[i]);
            } else if (type == double.class) {
                assertEquals(tuple.getDouble(i), values[i]);
            } else if (type.isArray()) {
                //noinspection unchecked
                assertTrue(getArrayHashingStrategy(type).equals(tuple.get(i), values[i])); // default hashing strategy for arrays
            } else {
                assertEquals(tuple.get(i), values[i]);
            }
        }
        assertEquals(tuple, tuple);
        assertNotNull(tuple.toString());
        return tuple;
    }

    private static HashingStrategy getArrayHashingStrategy(Class paramType) {
        if (paramType == boolean[].class) {
            return BooleanArrayHashingStrategy.getInstance();
        }
        if (paramType == byte[].class) {
            return ByteArrayHashingStrategy.getInstance();
        }
        if (paramType == char[].class) {
            return CharArrayHashingStrategy.getInstance();
        }
        if (paramType == short[].class) {
            return ShortArrayHashingStrategy.getInstance();
        }
        if (paramType == int[].class) {
            return IntArrayHashingStrategy.getInstance();
        }
        if (paramType == long[].class) {
            return LongArrayHashingStrategy.getInstance();
        }
        if (paramType == float[].class) {
            return FloatArrayHashingStrategy.getInstance();
        }
        if (paramType == double[].class) {
            return DoubleArrayHashingStrategy.getInstance();
        }
        if (paramType.isArray()) {
            return ArrayHashingStrategy.getInstance();
        }
        throw new UnsupportedOperationException();
    }

    @Test
    public void testIteration() throws Exception {
        TupleFactory f = TupleGenerator.createTupleFactory(new HashingStrategy[4], int.class, String.class, Object.class, long.class);
        Tuple t = f.create(3, "123", 3L, 4L);
        List<Object> l = new ArrayList<Object>();
        for (Object o : t) {
            l.add(o);
        }
        Assert.assertEquals(l, Arrays.asList(3, "123", 3L, 4L));
    }

    @Test
    public void test1Tuple() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class[] classes = {String.class, int[].class, int.class, short.class, long.class, byte.class, float.class, double.class, boolean.class, char.class};
        Object[] boxedVals = {"a", new int[]{34}, (int)1, (short)2, (long)3, (byte)4, (float)5.0, (double)6.0, false, '8'};
        for (int i = 0; i < classes.length; i++) {
            Class clazz = classes[i];
            Object boxedValue = boxedVals[i];
            Class<Tuple> tupleClass = TupleGenerator.getTupleClass(clazz);
            Assert.assertEquals(1, tupleClass.getConstructors().length);
            Constructor<?> ctor = tupleClass.getConstructors()[0];
            Tuple t = (Tuple) ctor.newInstance(new HashingStrategy[]{null}, boxedValue);
            Assert.assertEquals(t.size(), 1);
            Assert.assertEquals(t.get(0), boxedValue);
            Iterator<Object> it = t.iterator();
            Assert.assertEquals(boxedValue, it.next());
            Assert.assertFalse(it.hasNext());

            java.lang.reflect.Method getter = t.getClass().getMethod("getElement0"); // generated method
            Assert.assertEquals(boxedValue, getter.invoke(t));
        }
    }
}
