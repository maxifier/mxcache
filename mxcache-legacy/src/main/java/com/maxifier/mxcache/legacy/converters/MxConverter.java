package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.PublicAPI;

public interface MxConverter<F, T> {
    T convert(F t) throws ConverterException;

}
