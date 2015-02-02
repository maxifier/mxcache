/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.provider.Signature;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class ScalarTransformGenerator implements TransformGenerator {
    @Override
    public Signature transformKey(Signature in) {
        if (in.getContainer() == null) {
            throw new IllegalArgumentException("Cannot transform signature without key!");
        }
        int keyCount = in.getKeyCount();
        if (keyCount != 1) {
            throw new UnsupportedOperationException("Scalar transform cannot be applied to tuple");
        }
        return in.overrideKey(getOutType());
    }

    @Override
    public Signature transformValue(Signature in) {
        return in.overrideValue(getOutType());
    }
}
