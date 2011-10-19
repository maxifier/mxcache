package com.maxifier.mxcache.impl.caches.abs.elementlocked;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.CacheId;
import com.maxifier.mxcache.impl.CalculatableHelper;
import com.maxifier.mxcache.impl.resource.*;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.storage.elementlocked.*;

import org.jetbrains.annotations.NotNull;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:29:47
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public abstract class AbstractCharacterObjectCache<F> extends AbstractElementLockedCache implements CharacterObjectCache<F>, CharacterObjectElementLockedStorage<F> {
    private final CharacterObjectCalculatable<F> calculatable;

    @NotNull
    private final DependencyNode node;

    private final Object owner;

    public AbstractCharacterObjectCache(Object owner, CharacterObjectCalculatable<F> calculatable, @NotNull DependencyNode node, MutableStatistics statistics) {
        super(statistics);
        this.node = node;
        this.owner = owner;
        this.calculatable = calculatable;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public F getOrCreate(char o) {
        lock(o);
        try {
            Object v = load(o);
            if (v != UNDEFINED) {
                DependencyTracker.mark(node);
                hit();
                return (F)v;
            }
            DependencyNode callerNode = DependencyTracker.track(node);
            try {
                while(true) {
                    try {
                        return create(o);
                    } catch (ResourceOccupied e) {
                        if (callerNode != null) {
                            throw e;
                        } else {
                            unlock(o);
                            try {
                                e.getResource().waitForEndOfModification();
                            } finally {
                                lock(o);
                            }
                            v = load(o);
                            if (v != UNDEFINED) {
                                hit();
                                return (F)v;
                            }
                        }
                    }
                }
            } finally {
                DependencyTracker.exit(callerNode);
            }
        } finally {
            unlock(o);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private F create(char key) {
        long start = System.nanoTime();
        F t = calculatable.calculate(owner, key);
        long end = System.nanoTime();
        miss(end - start);
        save(key, t);
        return t;
    }

    @Override
    public CacheDescriptor getDescriptor() {
        CacheId id = CalculatableHelper.getId(calculatable.getClass());
        return CacheFactory.getProvider().getDescriptor(id);
    }

    @Override
    public DependencyNode getDependencyNode() {
        return node;
    }

    @Override
    public String toString() {
        return getDescriptor() + ": " + owner;
    }
}
