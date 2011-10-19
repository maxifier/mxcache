package com.maxifier.mxcache.legacy.converters;

import com.magenta.dataserializator.MxDataSerializator;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 10:55:53
 */
public class MxConvertFromByteArray10<T> implements MxConverter<byte[], T> {
    private final MxDataSerializator dataSerializator;

    public MxConvertFromByteArray10(MxDataSerializator dataSerializator) {
        this.dataSerializator = dataSerializator;
    }

    @Override
    public T convert(byte[] bytes) {
        try {
            //noinspection RedundantTypeArguments
            return dataSerializator.<T>deserialize(bytes);
        } catch (IOException e) {
            throw new ConverterException(e);
        } catch (ClassNotFoundException e) {
            throw new ConverterException(e);
        }
    }
}