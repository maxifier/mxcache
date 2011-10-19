package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.proxy.Resolvable;

/**
 * Project: Smart Advertising
 * Created by: Yakoushin Andrey
 * Date: 12.12.2008
 * Time: 13:29:09
 * <p/>
 * Copyright (c) 1999-2008 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public class MxCacheProxy<T> {
    private final Resolvable<T> element;

    public MxCacheProxy(Resolvable<T> element) {
        this.element = element;
    }

    public T getValue() {
        return element.getValue();
    }

    public Resolvable<T> getElement() {
        return element;
    }
}
