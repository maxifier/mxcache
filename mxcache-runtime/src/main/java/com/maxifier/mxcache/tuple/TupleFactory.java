/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.tuple;

/**
 * TupleFactory - generates tuple instances with specific types.
 *
 * @see TupleGenerator
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface TupleFactory {
    Tuple create(Object... values);

    Class<? extends Tuple> getTupleClass();
}
