package com.maxifier.mxcache.tuple;

import com.maxifier.mxcache.tuple.Tuple;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 15.09.2010
* Time: 15:43:50
*/
public interface TupleFactory {
    Tuple create(Object... values);

    Class<? extends Tuple> getTupleClass();
}
