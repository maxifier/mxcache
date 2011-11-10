package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.PublicAPI;

/**
* Created by IntelliJ IDEA.
* User: kochurov
* Date: 10.11.11
* Time: 13:39
*/
@PublicAPI
public class IdentityConverter implements MxConverter {
    public IdentityConverter() {}

    private static final IdentityConverter INSTANCE = new IdentityConverter();

    @SuppressWarnings({"unchecked"})
    public static <T> MxConverter<T, T> getInstance() {
        return INSTANCE;
    }

    @Override
    public Object convert(Object t) throws ConverterException {
        return t;
    }
}
