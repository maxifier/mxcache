/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.tuple;

import gnu.trove.strategy.HashingStrategy;
import gnu.trove.strategy.IdentityHashingStrategy;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
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

    public void testObject() throws Exception {
        check(array(String.class, Comparable.class, Object.class), "Test", 4, null);
    }

    public void testMixed() throws Exception {
        check(array(String.class, int.class), "Test", 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidParam() throws Exception {
        createTuple(array(String.class), "Test").get(2);
    }

    public void testCustomStrategy() throws Exception {
        IdentityHashingStrategy<String> strategy = new IdentityHashingStrategy<String>();
        Tuple tuple = createTuple(array(String.class, int.class), "Test", 3);
        // у int hashCode равен ему самому
        int sample = Arrays.hashCode(new int[] { System.identityHashCode("Test"), 3});
        assert tuple.hashCode(strategy, null) == sample;

        Tuple tuple2 = createTuple(array(String.class, int.class), "tEST", 3);
        assert tuple.equals(tuple2, new HashingStrategy<String>() {
            @Override
            public int computeHashCode(String object) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean equals(String o1, String o2) {
                return o1.equalsIgnoreCase(o2);
            }
        }, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testIncompatibleStrategy() throws Exception {
        Tuple tuple = createTuple(array(int.class), 3);
        Tuple tuple2 = createTuple(array(int.class), 4);
        assert tuple.equals(tuple2, new IdentityHashingStrategy());
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testIncompatibleStrategyForBoolean() throws Exception {
        Tuple tuple = createTuple(array(boolean.class), true);
        Tuple tuple2 = createTuple(array(boolean.class), false);
        assert tuple.equals(tuple2, new IdentityHashingStrategy());
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testInvalidTypeInt() throws Exception {
        createTuple(array(String.class), "Test").getInt(0);
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testInvalidTypeLong() throws Exception {
        createTuple(array(String.class), "Test").getLong(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidConverts() throws Exception {
        createTuple(array(long.class), 3L).getInt(0);
    }

    public void testConverts() throws Exception {
        Tuple tuple = createTuple(array(byte.class, int.class, float.class, String.class), (byte)1, 3, 4f, "test");
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

    public void testEquals() throws Exception {
        Class[] t = array(boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, float.class, double.class, double.class, String.class, String.class);
        Object[] v1 = { true, (byte) 3, 'a', (short) 11, 441, 71L, 31f, Float.NaN, 44d, Double.NaN, "123", null};
        Object[] v2 = { false, (byte) 4, '7', (short) 12, 421, 33L, 4.5f, 0f, 0d, 3.1d, "321", "124365"};

        Tuple t0 = createTuple(t, v1);
        Tuple t0c = createTuple(t, v1);

        assertTrue(t0.equals(t0));
        assertTrue(t0.equals(t0c));
        assertTrue(t0c.equals(t0));
        assertTrue(t0c.equals(t0c));

        int n = t.length;
        int N = 1 << n;
        Object[] s = new Object[n];
        for (int i = 1; i < N; i++) {
            for (int b = 0, m = i; b < n; b++, m >>= 1) {
                s[b] = (m & 1) == 0 ? v1[b] : v2[b];
            }
            Tuple t1 = createTuple(t, s);
            assertFalse(t0.equals(t1));
            assertFalse(t1.equals(t0));
            assertTrue(t1.equals(t1));
        }
    }

    private static Tuple check(Class[] types, Object... values) throws Exception {
        assert types.length == values.length;
        Tuple tuple = createTuple(types, values);
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
            }
        }
        assertTrue(Arrays.equals(tuple.toArray(), values));
        assertEquals(tuple.hashCode(), Arrays.hashCode(values));
        assertEquals(tuple, tuple);
        assertNotNull(tuple.toString());
        return tuple;
    }

    private static Tuple createTuple(Class[] types, Object... values) throws Exception {
        TupleFactory factory = TupleGenerator.getTupleFactory(types);
        Class<? extends Tuple> type = factory.getTupleClass();
        assertEquals(type.getClassLoader(), ClassLoader.getSystemClassLoader());
        assertEquals(Class.forName(type.getName()), type);
        return factory.create(values);
    }

    public void testIteration() throws Exception {
        Tuple t = createTuple(new Class[]{int.class, String.class, Object.class, long.class}, 3, "123", 3L, 4L);
        List<Object> l = new ArrayList<Object>();
        for (Object o : t) {
            l.add(o);
        }
        Assert.assertEquals(l, Arrays.asList(3, "123", 3L, 4L));
    }
    
    public void testSingletonFactoryMxcache30() {
        TupleFactory f1 = TupleGenerator.getTupleFactory(int.class, int.class);
        TupleFactory f2 = TupleGenerator.getTupleFactory(int.class, int.class);
        Assert.assertSame(f1, f2);

        TupleFactory f3 = TupleGenerator.getTupleFactory(int.class, String.class);
        TupleFactory f4 = TupleGenerator.getTupleFactory(int.class, String.class);
        Assert.assertSame(f3, f4);

        TupleFactory f5 = TupleGenerator.getTupleFactory(int.class, Object.class);
        Assert.assertSame(f3, f5);
    }
}
