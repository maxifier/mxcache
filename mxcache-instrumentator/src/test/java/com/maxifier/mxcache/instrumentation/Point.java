/* Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 * Maxifier Ltd  proprietary and confidential.
 * Use is subject to license terms.
 */
package com.maxifier.mxcache.instrumentation;

import java.io.Serializable;

/**
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (29.05.13)
 */
public interface Point extends Serializable {
    void setY(long y);

    void setX(long x);

    void setNewXY(long x, long y);

    double getRadius();
}
