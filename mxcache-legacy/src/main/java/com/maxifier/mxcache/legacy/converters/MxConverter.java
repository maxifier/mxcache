package com.maxifier.mxcache.legacy.converters;

public interface MxConverter<F, T> {
    T convert(F t) throws ConverterException;
}
