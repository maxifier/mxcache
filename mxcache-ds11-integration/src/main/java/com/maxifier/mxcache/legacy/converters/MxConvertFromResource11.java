package com.maxifier.mxcache.legacy.converters;

import com.magenta.dataserializator.link.LinkContext;
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
public class MxConvertFromResource11<T> implements MxConverter<MxResource, T> {
    private static final Logger logger = LoggerFactory.getLogger(MxConvertFromResource11.class);

    private final MxDataSerializator dataSerializator;
    private final LinkContext context;

    public MxConvertFromResource11(MxDataSerializator dataSerializator) {
        this(dataSerializator, LinkContext.EMPTY);
    }

    public MxConvertFromResource11(MxDataSerializator dataSerializator, LinkContext context) {
        this.dataSerializator = dataSerializator;
        this.context = context;
    }

    @Override
    public T convert(MxResource resource) {
        if (!resource.exists()) {
            logger.error("Resource doesn't exist " + resource);
            return null;
        }
        try {
            //noinspection RedundantTypeArguments
            return dataSerializator.<T>deserialize(new BufferedInputStream(resource.getInputStream()), context);
        } catch (Exception e) {
            logger.error("Cannot load resource " + resource, e);
            throw new ConverterException(e);
        }
    }

}