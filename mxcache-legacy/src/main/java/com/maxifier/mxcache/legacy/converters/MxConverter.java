package com.maxifier.mxcache.legacy.converters;

public interface MxConverter<F, T> {
    T convert(F t) throws ConverterException;

    MxConverter IDENTITY = new MxConverter() {
        @Override
        public Object convert(Object t) throws ConverterException {
            return t;
        }
    };
}
