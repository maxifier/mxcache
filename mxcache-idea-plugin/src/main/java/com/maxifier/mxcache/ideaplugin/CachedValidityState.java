/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import com.intellij.openapi.compiler.ValidityState;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataOutput;
import java.io.DataInput;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
final class CachedValidityState implements ValidityState {
    private final long stamp;

    public CachedValidityState(DataInput input) throws IOException {
        this(input.readLong());
    }

    public CachedValidityState(long stamp) {
        this.stamp = stamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return equalsTo((CachedValidityState) o);

    }

    @Override
    public int hashCode() {
        return (int) (stamp ^ (stamp >>> 32));
    }

    @Override
    public boolean equalsTo(ValidityState validityState) {
        if (validityState instanceof CachedValidityState) {
            return stamp == ((CachedValidityState)validityState).stamp;
        }
        return false;
    }

    /// theese two methods are used in different versions of intellij idea openapi /////////////////////////////////////
    /// be careful, don't put @Override annotations here - it may break compilation ////////////////////////////////////

    // this method is used in Idea 7 and earlier
    public void save(DataOutputStream dataOutputStream) throws IOException {
        save((DataOutput) dataOutputStream);
    }

    // this method is used in Idea 8 and later
    public void save(DataOutput dataOutputStream) throws IOException {
        dataOutputStream.writeLong(stamp);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
