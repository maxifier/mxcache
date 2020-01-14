/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.ResourceView;
import com.maxifier.mxcache.resource.MxResource;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aleksey Tomin (aleksey.tomin@cxense.com) (2016-10-21)
 */
public class DependencyTrackerInvaldateCacheUTest {

    private final Entity entity = new Entity();

    @Test
    public void testVal1() {
        Assert.assertEquals(getVal1(), 10);
        // The cache doesn't invalidated, because no dependencies control
        entity.setVal1(11);
        Assert.assertEquals(getVal1(), 10);
    }

    @Test
    public void testVal2() {
        Assert.assertEquals(getVal2(), 20);
        // The cache doesn't invalidated, because no dependencies control
        entity.setVal2(21);
        Assert.assertEquals(getVal2(), 20);
    }

    @Test
    public void testVal3() {
        Assert.assertEquals(getVal3(), 30);
        // The cache is invalidated by using MxResource
        entity.setVal3(31);
        Assert.assertEquals(getVal3(), 31);
    }

    @Test
    public void testVal4() {
        Assert.assertEquals(getVal4(), 40);
        // The cache is invalidated by using MxResource
        entity.setVal4(41);
        Assert.assertEquals(getVal4(), 41);
    }

    @Cached
    public int getVal1() {
        return entity.getVal1();
    }

    @Cached
    public int getVal2() {
        return entity.getVal2();
    }

    @Cached
    public int getVal3() {
        return entity.getVal3();
    }

    @Cached
    public int getVal4() {
        return entity.getVal4();
    }

}

class Entity {
    private static final MxResource VAL3_RESOURCE = MxResourceFactory.getResource("val3");
    private static final MxResource VAL4_RESOURCE = MxResourceFactory.getResource("val4");

    private int val1 = 10;
    private int val2 = 20;
    private int val3 = 30;
    private int val4 = 40;

    public int getVal1() {
        return val1;
    }

    public void setVal1(int val1) {
        this.val1 = val1;
    }

    @Cached
    @ResourceView
    public int getVal2() {
        return val2;
    }

    public void setVal2(int val2) {
        this.val2 = val2;
    }

    public int getVal3() {
        VAL3_RESOURCE.readStart();
        try {
            return val3;
        } finally {
            VAL3_RESOURCE.readEnd();
        }
    }

    public void setVal3(int val3) {
        VAL3_RESOURCE.writeStart();
        try {
            this.val3 = val3;
        } finally {
            VAL3_RESOURCE.writeEnd();
        }
    }

    @Cached
    @ResourceView
    public int getVal4() {
        VAL4_RESOURCE.readStart();
        try {
            return val4;
        } finally {
            VAL4_RESOURCE.readEnd();
        }
    }

    public void setVal4(int val4) {
        VAL4_RESOURCE.writeStart();
        try {
            this.val4 = val4;
        } finally {
            VAL4_RESOURCE.writeEnd();
        }
    }
}
