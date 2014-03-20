/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.MxResource;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * MxCacheUTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2013-10-02 13:49)
 */
@Test
public class MxCacheUTest {
    public void testHideClientDependencies() {
        final AtomicBoolean cleaned = new AtomicBoolean();
        final MxResource resource = MxResourceFactory.createResource(this, "test");
        DependencyTrackingAction action = new DependencyTrackingAction() {
            @Override
            protected void onClear() {
                cleaned.set(true);
            }
        };
        final AtomicBoolean initialized = new AtomicBoolean();
        action.trackDependencies(new Runnable() {
            @Override
            public void run() {
                MxCache.hideCallerDependencies(new Runnable() {
                    @Override
                    public void run() {
                        resource.readStart();
                        try {
                            initialized.set(true);
                        } finally {
                            resource.readEnd();
                        }
                    }
                });
            }
        });
        assertTrue(initialized.get());
        resource.clearDependentCaches();
        assertFalse(cleaned.get());

        initialized.set(false);

        action.trackDependencies(new Runnable() {
            @Override
            public void run() {
                resource.readStart();
                try {
                    initialized.set(true);
                } finally {
                    resource.readEnd();
                }
            }
        });

        assertTrue(initialized.get());
        resource.clearDependentCaches();
        assertTrue(cleaned.get());
    }
}
