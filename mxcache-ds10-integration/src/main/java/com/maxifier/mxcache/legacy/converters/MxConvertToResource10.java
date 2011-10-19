package com.maxifier.mxcache.legacy.converters;

import com.magenta.dataserializator.MxDataSerializator;
import com.maxifier.mxcache.legacy.MxResource;
import com.maxifier.mxcache.legacy.MxResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 10:59:56
 */
public class MxConvertToResource10<T> extends MxAbstractResourceConverter<T> {
    private static final Logger logger = LoggerFactory.getLogger(MxConvertToResource10.class);

    private final MxDataSerializator dataSerializator;

    public MxConvertToResource10(MxResourceManager resourceManager, MxDataSerializator dataSerializator) {
        super(resourceManager);
        this.dataSerializator = dataSerializator;
    }

    public MxConvertToResource10(MxResourceManager resourceManager, MxDataSerializator dataSerializator, String resourceNameTemplate) {
        super(resourceManager, resourceNameTemplate);
        this.dataSerializator = dataSerializator;
    }

    @Override
    public MxResource convert(T t) {
        if (t == null) {
            return null;
        }
        MxResource res = createUniqueCacheFile(t.getClass());
        try {
            dataSerializator.serialize(t, new BufferedOutputStream(res.getOutputStream(false)));
            return res;
        } catch (Exception e) {
            logger.error("Cannot save forecast: cannot write resource " + res, e);
            throw new ConverterException(e);
        }
    }
}