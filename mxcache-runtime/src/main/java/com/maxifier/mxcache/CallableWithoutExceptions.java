/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import java.util.concurrent.Callable;

/**
* CallableWithoutExceptions
*
* @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2013-10-02 14:55)
*/
public interface CallableWithoutExceptions<T> extends Callable<T> {
    T call();
}
