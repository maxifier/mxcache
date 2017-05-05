/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.lib;

import java.util.List;

/**
 * InterfaceA
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-07 11:29)
 */
public interface InterfaceA<T extends Appendable> {
    T get();

    <R extends Integer, Z extends Throwable & Runnable> R genericMethod(R input) throws Z;

    List<? super T> getList();
}
