package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.Cache;

import java.util.List;

/**
 * Project: Maxifier
* Created by: Yakoushin Andrey
* Date: 15.02.2010
* Time: 13:13:29
* <p/>
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*
* @author ELectronic ENgine
*/
public final class ClassCacheIds {
    private final int[] instanceIds;
    private final int[] staticIds;

    public ClassCacheIds(int[] instanceIds, int[] staticIds) {
        this.instanceIds = instanceIds;
        this.staticIds = staticIds;
    }

    public int[] getInstanceIds() {
        return instanceIds;
    }

    public int[] getStaticIds() {
        return staticIds;
    }

    @SuppressWarnings({ "unchecked" })
    void appendInstanceCaches(Cleanable cleanable, Object instance, List<Cache> list) {
        for (int id : instanceIds) {
            list.add(cleanable.getInstanceCache(instance, id));
        }
    }
}
