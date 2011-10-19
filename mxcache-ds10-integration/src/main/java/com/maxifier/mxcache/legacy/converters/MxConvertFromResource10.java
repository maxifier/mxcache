package com.maxifier.mxcache.legacy.converters;

import com.magenta.dataserializator.MxDataSerializator;
import com.maxifier.mxcache.legacy.MxResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 10:59:56
 */
public class MxConvertFromResource10<T> implements MxConverter<MxResource, T> {
    private static final Logger logger = LoggerFactory.getLogger(MxConvertFromResource10.class);

    private final MxDataSerializator dataSerializator;

    public MxConvertFromResource10(MxDataSerializator dataSerializator) {
        this.dataSerializator = dataSerializator;
    }

    @Override
    public T convert(MxResource resource) {
        if (!resource.exists()) {
            logger.error("Resource doesn't exist " + resource);
            return null;
        }
        try {
            //noinspection RedundantTypeArguments
            return dataSerializator.<T>deserialize(new BufferedInputStream(resource.getInputStream()));
        } catch (Exception e) {
            logger.error("Cannot load resource " + resource, e);
            throw new ConverterException(e);
        }
    }
}