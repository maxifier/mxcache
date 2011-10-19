package com.maxifier.mxcache.legacy.converters;

import com.magenta.dataserializator.link.LinkContext;
import com.magenta.dataserializator.MxDataSerializator;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 10:55:53
 */
public class MxConvertFromByteArray11<T> implements MxConverter<byte[], T> {
    private final MxDataSerializator dataSerializator;
    private final LinkContext context;

    public MxConvertFromByteArray11(MxDataSerializator dataSerializator) {
        this(dataSerializator, LinkContext.EMPTY);
    }

    public MxConvertFromByteArray11(MxDataSerializator dataSerializator, LinkContext context) {
        this.dataSerializator = dataSerializator;
        this.context = context;
    }

    @Override
    public T convert(byte[] bytes) {
        try {
            //noinspection RedundantTypeArguments
            return dataSerializator.<T>deserialize(bytes, context);
        } catch (IOException e) {
            throw new ConverterException(e);
        } catch (ClassNotFoundException e) {
            throw new ConverterException(e);
        }
    }
}