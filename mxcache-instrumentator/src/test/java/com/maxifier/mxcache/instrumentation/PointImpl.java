/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.ResourceView;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.resource.TrackDependency;

/**
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (28.05.13)
 */
public class PointImpl implements Point {
    public long x;
    public long y;

    protected final MxResource xyRes = MxResourceFactory.getResource("xy");

    public PointImpl() {
    }

    public PointImpl(long x, long y) {
        xyRes.writeStart();
        try {
            this.x = x;
            this.y = y;
        } finally {
            xyRes.writeEnd();
        }
    }


    @Override
    public void setY(long y) {
        xyRes.writeStart();
        try {
            this.y = y;
        } finally {
            xyRes.writeEnd();
        }
    }

    @Override
    public void setX(long x) {
        xyRes.writeStart();
        try {
            this.x = x;
        } finally {
            xyRes.writeEnd();
        }
    }

    @Override
    public void setNewXY(long x, long y) {
        xyRes.writeStart();
        try {
            this.x = x;
            this.y = y;
        } finally {
            xyRes.writeEnd();
        }
    }

    @Cached(tags = "param")
    @TrackDependency(DependencyTracking.INSTANCE)
    public long getX() {
        xyRes.readStart();
        try {
            return x;
        } finally {
            xyRes.readEnd();
        }
    }

    @Cached(tags = "param")
    @TrackDependency(DependencyTracking.INSTANCE)
    public long getY() {
        xyRes.readStart();
        try {
            return y;
        } finally {
            xyRes.readEnd();
        }
    }

    @Override
    @Cached
    @TrackDependency(DependencyTracking.INSTANCE)
    @ResourceView
    public double getRadius() {
        long x1 = getX();
        long y1 = getY();
        return Math.sqrt(x1 * x1 + y1 * y1);
    }

}
