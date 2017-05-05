/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.lib;

/**
 * RecursiveGeneric
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-09 17:25)
 */
public class RecursiveGeneric<T extends RecursiveGeneric<T>> {
    public T get() {
        return null;
    }

    public <T extends RecursiveGeneric<T>, S extends RecursiveGeneric<T>> S complexMethod() {
        return null;
    }
}
