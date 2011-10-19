package com.maxifier.mxcache.legacy.converters;

import com.magenta.dataserializator.MxDataSerializator;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 10:55:53
 */
public class MxConvertToByteArray10<T> implements MxConverter<T, byte[]> {
    private final MxDataSerializator dataSerializator;

    public MxConvertToByteArray10(MxDataSerializator dataSerializator) {
        this.dataSerializator = dataSerializator;
    }

    @Override
    public byte[] convert(T t) {
        try {
            return dataSerializator.serialize(t);
        } catch (IOException e) {
            throw new ConverterException(e);
        }
    }
}
