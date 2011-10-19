package com.maxifier.mxcache.jconsoleplugin;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 16.08.2010
 * Time: 10:14:12
 */
public class ComparableComparator<T extends Comparable> implements Comparator<T>, Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings({ "unchecked" })
    @Override
    public int compare(Comparable o1, Comparable o2) {
        if (o1 == null) {
            return o2 == null ? -1 : 0;
        }
        if (o2 == null) {
            return 1;
        }
        return o1.compareTo(o2);
    }
}
