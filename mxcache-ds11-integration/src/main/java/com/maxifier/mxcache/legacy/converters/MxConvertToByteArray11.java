package com.maxifier.mxcache.legacy.converters;

import com.magenta.dataserializator.MxDataSerializator;
import com.magenta.dataserializator.MxFlagOption;
import com.magenta.dataserializator.MxOptionSet;
import com.magenta.dataserializator.MxOptionSetBuilder;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 10:55:53
 */
public class MxConvertToByteArray11<T> implements MxConverter<T, byte[]> {
    private final MxDataSerializator dataSerializator;
    private final MxOptionSet optionsSet;

    public MxConvertToByteArray11(MxDataSerializator dataSerializator) {
        this(dataSerializator, MxOptionSet.EMPTY);
    }

    public MxConvertToByteArray11(MxDataSerializator dataSerializator, MxFlagOption... options) {
        this(dataSerializator, new MxOptionSetBuilder().with(options).toOptionSet());
    }

    public MxConvertToByteArray11(MxDataSerializator dataSerializator, MxOptionSet optionsSet) {
        this.dataSerializator = dataSerializator;
        this.optionsSet = optionsSet;
    }

    @Override
    public byte[] convert(T t) {
        try {
            return dataSerializator.serialize(t, optionsSet);
        } catch (IOException e) {
            throw new ConverterException(e);
        }
    }
}
