/*
 * Copyright (c) 2008-2016 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import static org.testng.Assert.assertEquals;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.ResourceView;
import com.maxifier.mxcache.StatisticsMode;
import com.maxifier.mxcache.StatisticsModeEnum;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.resource.TrackDependency;

import org.testng.annotations.Test;

/**
 * @author Created by Aleksey Tomin (aleksey.tomin@cxense.com) (2016-06-10)
 */
public class ResourceDependencyTrackingUTest {

    @Test
    public void testDependencyFromEntity() {
        Entity e1 = new Entity("1", 11);
        Entity e2 = new Entity("2", 21);
        Service s = new Service();

        assertEquals(s.getVal(e1), 11);
        assertEquals(s.getVal(e2), 21);

        e1.setValDirect(12);
        assertEquals(s.getVal(e1), 11);
        assertEquals(s.getVal(e2), 21);

        e2.setVal(22);
        assertEquals(s.getVal(e1), 11);
        assertEquals(s.getVal(e2), 22);

        e1.setVal(13);
        assertEquals(s.getVal(e1), 13);
        assertEquals(s.getVal(e2), 22);
    }

}

class Entity {
    private final MxResource res;

    private int val;

    Entity(String name, int val) {
        this.res = MxResourceFactory.getResource("Entity#" + name);
        this.val = val;
    }

    @Cached
    @TrackDependency(DependencyTracking.RESOURCE)
    @ResourceView
    public int getVal() {
        res.readStart();
        try {
            return val;
        } finally {
            res.readEnd();
        }
    }

    public void setVal(int val) {
        res.writeStart();
        try {
            this.val = val;
        } finally {
            res.writeEnd();
        }
    }

    public void setValDirect(int val) {
        this.val = val;
    }
}

class Service {
    @Cached
    public int getVal(Entity entity) {
        return entity.getVal();
    }
}

