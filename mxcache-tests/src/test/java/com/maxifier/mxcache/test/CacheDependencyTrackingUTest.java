package com.maxifier.mxcache.test;

import com.maxifier.mxcache.*;
import com.maxifier.mxcache.impl.caches.def.ObjectStorageImpl;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.resource.TrackDependency;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 17.06.11
 * Time: 13:49
 */
@Test
public class CacheDependencyTrackingUTest {
    static class X {
        int x;

        @Cached(tags = "x_dep", group = "x_group")
        @TrackDependency(DependencyTracking.INSTANCE)
        private int a() {
            return x;
        }
    }

    X x = new X();

    @Cached
    @TrackDependency(DependencyTracking.INSTANCE)
    private int b() {
        return x.a();
    }

    @BeforeMethod
    private void reset() {
        x.x = 0;
        CacheFactory.getCleaner().clearCacheByInstances(x, this);
    }

    public void testClearByTag() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearCacheByTag("x_dep");

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearByGroup() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearCacheByGroup("x_group");

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearInstanceByTag() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearInstanceByTag(x, "x_dep");

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearInstanceByGroup() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearInstanceByGroup(x, "x_group");

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearByInstance() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearCacheByInstance(x);

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearByClass() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearCacheByClass(X.class);

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    @Test
    public void testClearByTagForViewDouble() {
        PointImpl point = new PointImpl(3L, 4L);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getRadius(), 5.0);
        Assert.assertEquals(point.getViewCount1(), 1);

        CacheFactory.getCleaner().clearCacheByTag("param");
        Assert.assertEquals(point.getViewCount1(), 1);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getRadius(), 5.0);
        Assert.assertEquals(point.getViewCount1(), 2);

        point.x = 4L;
        point.y = 3L;

        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getRadius(), 5.0);
        Assert.assertEquals(point.getViewCount1(), 2);

        CacheFactory.getCleaner().clearCacheByTag("param");
        Assert.assertEquals(point.getViewCount1(), 2);
        Assert.assertEquals(point.getX(), 4L);
        Assert.assertEquals(point.getY(), 3L);
        Assert.assertEquals(point.getRadius(), 5.0);
        Assert.assertEquals(point.getViewCount1(), 3);


        point.y = 0L;
        Assert.assertEquals(point.getY(), 3L);
        Assert.assertEquals(point.getRadius(), 5.0);
        Assert.assertEquals(point.getViewCount1(), 3);

        CacheFactory.getCleaner().clearCacheByTag("param");
        Assert.assertEquals(point.getViewCount1(), 3);
        Assert.assertEquals(point.getY(), 0L);
        Assert.assertEquals(point.getRadius(), 4.0);
        Assert.assertEquals(point.getViewCount1(), 4);
    }

    @Test
    public void testClearResourceForViewDouble() {
        PointImpl point = new PointImpl(3L, 4L);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getRadius(), 5.0);
        Assert.assertEquals(point.getViewCount1(), 1);

        point.setX(3L);
        Assert.assertEquals(point.getViewCount1(), 3);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getRadius(), 5.0);
        Assert.assertEquals(point.getViewCount1(), 3);

        point.setNewXY(4L, 3L);
        Assert.assertEquals(point.getViewCount1(), 5);
        Assert.assertEquals(point.getX(), 4L);
        Assert.assertEquals(point.getY(), 3L);
        Assert.assertEquals(point.getRadius(), 5.0);
        Assert.assertEquals(point.getViewCount1(), 5);

        point.setY(0L);
        Assert.assertEquals(point.getViewCount1(), 6);
        Assert.assertEquals(point.getY(), 0L);
        Assert.assertEquals(point.getRadius(), 4.0);
        Assert.assertEquals(point.getViewCount1(), 7);
    }

    @Test
    public void testClearForViewLong() {
        PointImpl point = new PointImpl(3L, 4L);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getPerimeter(), 14L);
        Assert.assertEquals(point.getViewCount2(), 1);

        point.setX(3L);
        Assert.assertEquals(point.getViewCount2(), 3);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getPerimeter(), 14L);
        Assert.assertEquals(point.getViewCount2(), 3);

        point.setNewXY(4L, 3L);
        Assert.assertEquals(point.getViewCount2(), 5);
        Assert.assertEquals(point.getX(), 4L);
        Assert.assertEquals(point.getY(), 3L);
        Assert.assertEquals(point.getPerimeter(), 14L);
        Assert.assertEquals(point.getViewCount2(), 5);

        point.setY(0L);
        Assert.assertEquals(point.getViewCount2(), 6);
        Assert.assertEquals(point.getY(), 0L);
        Assert.assertEquals(point.getPerimeter(), 8L);
        Assert.assertEquals(point.getViewCount2(), 7);
    }


    @Test
    public void testClearForViewWithKey() {
        PointImpl point = new PointImpl(3L, 4L);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getRadius(0L), 5.0);
        Assert.assertEquals(point.getViewCount3(), 1);

        point.setX(3L);
        Assert.assertEquals(point.getViewCount3(), 1);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getRadius(0L), 5.0);
        Assert.assertEquals(point.getViewCount3(), 2);

        point.setNewXY(4L, 3L);
        Assert.assertEquals(point.getViewCount3(), 2);
        Assert.assertEquals(point.getX(), 4L);
        Assert.assertEquals(point.getY(), 3L);
        Assert.assertEquals(point.getRadius(0L), 5.0);
        Assert.assertEquals(point.getViewCount3(), 3);

        point.setY(0L);
        Assert.assertEquals(point.getViewCount3(), 3);
        Assert.assertEquals(point.getY(), 0L);
        Assert.assertEquals(point.getRadius(0L), 4.0);
        Assert.assertEquals(point.getViewCount3(), 4);
    }

    @Test
    public void testClearForViewObject() {
        PointImpl point = new PointImpl(3L, 4L);
        Assert.assertEquals(point.toString(), "PointImpl{radius=5.0}");
        Assert.assertEquals(point.getViewCount4(), 1);

        point.setX(3L);
        Assert.assertEquals(point.getViewCount4(), 3);
        Assert.assertEquals(point.toString(), "PointImpl{radius=5.0}");
        Assert.assertEquals(point.getViewCount4(), 3);

        point.setNewXY(4L, 3L);
        Assert.assertEquals(point.getViewCount4(), 5);
        Assert.assertEquals(point.toString(), "PointImpl{radius=5.0}");
        Assert.assertEquals(point.getViewCount4(), 5);

        point.setY(0L);
        Assert.assertEquals(point.getViewCount4(), 6);
        Assert.assertEquals(point.toString(), "PointImpl{radius=4.0}");
        Assert.assertEquals(point.getViewCount4(), 7);

        point.setNewXY(4L, 3L);
        Assert.assertEquals(point.getViewCount4(), 8);
        Assert.assertEquals(point.toString(), "PointImpl{radius=5.0}");
        Assert.assertEquals(point.getViewCount4(), 9);
    }

    @Test
    public void testClearDependentFromView() {
        PointImpl point = new PointImpl(3L, 4L);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getDiameter(), 10);
        Assert.assertEquals(point.getViewCount5(), 1);

        point.setX(3L);
        Assert.assertEquals(point.getViewCount5(), 1);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getDiameter(), 10);
        Assert.assertEquals(point.getViewCount5(), 1);

        point.setNewXY(4L, 3L);
        Assert.assertEquals(point.getViewCount5(), 1);
        Assert.assertEquals(point.getX(), 4L);
        Assert.assertEquals(point.getY(), 3L);
        Assert.assertEquals(point.getDiameter(), 10);
        Assert.assertEquals(point.getViewCount5(), 1);

        point.setY(0L);
        Assert.assertEquals(point.getViewCount5(), 1);
        Assert.assertEquals(point.getY(), 0L);
        Assert.assertEquals(point.getDiameter(), 8);
        Assert.assertEquals(point.getViewCount5(), 2);
    }


    @Test
    public void testClearNonStorageView() {
        PointImpl point = new PointImpl(3L, 4L);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getCircuit(), 31L);
        Assert.assertEquals(point.getViewCount6(), 1);

        point.setX(3L);
        Assert.assertEquals(point.getViewCount6(), 1);
        Assert.assertEquals(point.getX(), 3L);
        Assert.assertEquals(point.getY(), 4L);
        Assert.assertEquals(point.getCircuit(), 31L);
        Assert.assertEquals(point.getViewCount6(), 2);

        point.setNewXY(4L, 3L);
        Assert.assertEquals(point.getViewCount6(), 2);
        Assert.assertEquals(point.getX(), 4L);
        Assert.assertEquals(point.getY(), 3L);
        Assert.assertEquals(point.getCircuit(), 31L);
        Assert.assertEquals(point.getViewCount6(), 3);

        point.setY(0L);
        Assert.assertEquals(point.getViewCount6(), 3);
        Assert.assertEquals(point.getY(), 0L);
        Assert.assertEquals(point.getCircuit(), 25L);
        Assert.assertEquals(point.getViewCount6(), 4);
    }

    @Test
    public void testDoubleDependency() {
        PointStorage storage = new PointStorage();
        PointImpl p1 = storage.createPoint(4L, 3L);
        PointImpl p2 = storage.createPoint(0L, 3L);

        Assert.assertEquals(p1.getRadius(), 5.0);
        Assert.assertEquals(p2.getRadius(), 3.0);
        Assert.assertEquals(storage.getAverageRadius(), 4.0);
        Assert.assertTrue(p1.isGreaterAvgRadius());
        Assert.assertFalse(p2.isGreaterAvgRadius());
        Assert.assertEquals(p1.getViewCount7(), 1);
        Assert.assertEquals(p2.getViewCount7(), 1);

        storage.resource.writeStart();
        try {
            p1.setX(0L);
            Assert.assertEquals(p1.getViewCount7(), 1);
            Assert.assertEquals(p2.getViewCount7(), 1);
            Assert.assertEquals(p1.getRadius(), 3.0);
            Assert.assertEquals(p2.getRadius(), 3.0);
        } finally {
            storage.resource.writeEnd();
        }

        Assert.assertEquals(p1.getRadius(), 3.0);
        Assert.assertEquals(p2.getRadius(), 3.0);
        Assert.assertEquals(storage.getAverageRadius(), 3.0);
        Assert.assertEquals(p1.getViewCount7(), 2);
        Assert.assertEquals(p2.getViewCount7(), 2);

        Assert.assertFalse(p1.isGreaterAvgRadius());
        Assert.assertFalse(p2.isGreaterAvgRadius());
        Assert.assertEquals(p1.getViewCount7(), 3);
        Assert.assertEquals(p2.getViewCount7(), 2);
    }

    static class PointStorage {
        protected final MxResource resource = MxResourceFactory.getResource("storage");

        private List<PointImpl> points = new ArrayList<PointImpl>();

        public PointImpl createPoint(long x, long y) {
            resource.writeStart();
            try {
                PointImpl point = new PointImpl(x, y, this);
                points.add(point);
                return point;
            } finally {
                resource.writeEnd();
            }
        }

        public double getAverageRadius() {
            double radius = 0.0;
            resource.readStart();
            try {
                for (PointImpl point : points) {
                    radius += point.getRadius();
                }
                return radius / points.size();
            } finally {
                resource.readEnd();
            }
        }
    }

    static class PointImpl {
        protected final MxResource xyRes = MxResourceFactory.getResource("xy");
        private final PointStorage storage;

        private int viewCount1 = 0;
        private int viewCount2 = 0;
        private int viewCount3 = 0;
        private int viewCount4 = 0;
        private int viewCount5 = 0;
        private int viewCount6 = 0;
        private int viewCount7 = 0;

        public long x;
        public long y;

        public PointImpl(long x, long y) {
            this.storage = null;
            xyRes.writeStart();
            try {
                this.x = x;
                this.y = y;
            } finally {
                xyRes.writeEnd();
            }
        }

        public PointImpl(long x, long y, PointStorage storage) {
            this.storage = storage;
            xyRes.writeStart();
            try {
                this.x = x;
                this.y = y;
            } finally {
                xyRes.writeEnd();
            }
        }

        public int getViewCount1() {
            return viewCount1;
        }

        public int getViewCount2() {
            return viewCount2;
        }

        public int getViewCount3() {
            return viewCount3;
        }

        public int getViewCount4() {
            return viewCount4;
        }

        public int getViewCount5() {
            return viewCount5;
        }

        public int getViewCount6() {
            return viewCount6;
        }

        public int getViewCount7() {
            return viewCount7;
        }

        public void setY(long y) {
            xyRes.writeStart();
            try {
                this.y = y;
            } finally {
                xyRes.writeEnd();
            }
        }

        public void setX(long x) {
            xyRes.writeStart();
            try {
                this.x = x;
            } finally {
                xyRes.writeEnd();
            }
        }

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

        @Cached
        @TrackDependency(DependencyTracking.INSTANCE)
        @ResourceView
        public double getRadius() {
            long x1 = getX();
            long y1 = getY();
            viewCount1++;
            return Math.sqrt(x1 * x1 + y1 * y1);
        }

        @Cached
        @TrackDependency(DependencyTracking.INSTANCE)
        @ResourceView
        boolean isGreaterAvgRadius() {
            viewCount7++;
            return getRadius() - storage.getAverageRadius() > 0;
        }

        @Cached
        @TrackDependency(DependencyTracking.INSTANCE)
        public int getDiameter() {
            viewCount5++;
            return (int) (getRadius() * 2);
        }

        @Cached
        @TrackDependency(DependencyTracking.INSTANCE)
        @ResourceView
        @UseStorage(ObjectStorageImpl.class)
        public long getPerimeter() {
            long x1 = getX();
            long y1 = getY();
            viewCount2++;
            return 2 * (x1 + y1);
        }

        @Cached
        @TrackDependency(DependencyTracking.INSTANCE)
        @ResourceView
        public double getRadius(long z) {
            long x1 = getX();
            long y1 = getY();
            viewCount3++;
            return Math.sqrt(x1 * x1 + y1 * y1 + z * z);
        }

        @Cached
        @TrackDependency(DependencyTracking.INSTANCE)
        @Strategy(NonStorageStrategy.class)
        @ResourceView
        public long getCircuit() {
            long x1 = getX();
            long y1 = getY();
            viewCount6++;
            return (long) Math.floor(2 * Math.PI * Math.sqrt(x1 * x1 + y1 * y1));
        }

        @Cached
        @TrackDependency(DependencyTracking.INSTANCE)
        @ResourceView
        @Override
        public String toString() {
            long x1 = getX();
            long y1 = getY();
            viewCount4++;
            return "PointImpl{radius=" +
                    Math.sqrt(x1 * x1 + y1 * y1) +
                    '}';
        }
    }
}