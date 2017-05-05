/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.resource;

import com.maxifier.mxcache.Cached;
import org.testng.annotations.Test;

import java.io.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class SerializationTest {
    static class T1 implements Serializable {
        int i;

        @Cached
        public int get() {
            return i++;
        }

        private void readObject(ObjectInputStream out) throws IOException, ClassNotFoundException {
            out.defaultReadObject();
        }
    }

    static class T2 implements Serializable {
        int i;

        @Cached
        public int get() {
            return i++;
        }                          
    }

    static class T3 implements Externalizable {
        int i;

        /** externalizable should have public constructor */
        public T3() {
        }

        @Cached
        public int get() {
            return i++;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(i);
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            i = in.readInt();
        }
    }

    private <T> T writeAndRead(T o) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        //noinspection unchecked
        return (T) ois.readObject();
    }

    public void testWithReadObject() throws Exception {
        T1 o = new T1();
        assert o.get() == 0;
        assert o.get() == 0;
        T1 t = writeAndRead(o);
        assert t.get() == 1;
        assert t.get() == 1;
    }

    public void testWithoutReadObject() throws Exception {
        T2 o = new T2();
        assert o.get() == 0;
        assert o.get() == 0;
        T2 t = writeAndRead(o);
        assert t.get() == 1;
        assert t.get() == 1;
    }

    public void testExternalizable() throws Exception {
        T3 o = new T3();
        assert o.get() == 0;
        assert o.get() == 0;
        T3 t = writeAndRead(o);
        assert t.get() == 1;
        assert t.get() == 1;
    }
}
