package com.maxifier.mxcache.legacy.converters;

import com.magenta.dataserializator.MxDataSerializator;
import com.magenta.dataserializator.MxFlagOption;
import com.magenta.dataserializator.MxOptionSet;
import com.magenta.dataserializator.MxOptionSetBuilder;
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
public class MxConvertToResource11<T> extends MxAbstractResourceConverter<T> {
    private static final Logger logger = LoggerFactory.getLogger(MxConvertToResource11.class);

    private final MxDataSerializator dataSerializator;
    private final MxOptionSet optionsSet;

    public MxConvertToResource11(MxResourceManager resourceManager, MxDataSerializator dataSerializator) {
        this(resourceManager, dataSerializator, MxOptionSet.EMPTY);
    }

    public MxConvertToResource11(MxResourceManager resourceManager, MxDataSerializator dataSerializator,  String resourceNameTemplate) {
        this(resourceManager, dataSerializator, resourceNameTemplate, MxOptionSet.EMPTY);
    }

    public MxConvertToResource11(MxResourceManager resourceManager, MxDataSerializator dataSerializator, MxFlagOption... options) {
        this(resourceManager, dataSerializator, new MxOptionSetBuilder().with(options).toOptionSet());
    }

    public MxConvertToResource11(MxResourceManager resourceManager, MxDataSerializator dataSerializator, String resourceNameTemplate, MxFlagOption... options) {
        this(resourceManager, dataSerializator, resourceNameTemplate, new MxOptionSetBuilder().with(options).toOptionSet());
    }

    public MxConvertToResource11(MxResourceManager resourceManager, MxDataSerializator dataSerializator, MxOptionSet optionsSet) {
        super(resourceManager);
        this.dataSerializator = dataSerializator;
        this.optionsSet = optionsSet;
    }

    public MxConvertToResource11(MxResourceManager resourceManager, MxDataSerializator dataSerializator, String resourceNameTemplate, MxOptionSet optionsSet) {
        super(resourceManager, resourceNameTemplate);
        this.dataSerializator = dataSerializator;
        this.optionsSet = optionsSet;
    }

    @Override
    public MxResource convert(T t) {
        if (t == null) {
            return null;
        }
        MxResource res = createUniqueCacheFile(t.getClass());
        try {
            dataSerializator.serialize(t, new BufferedOutputStream(res.getOutputStream(false)), optionsSet);
            return res;
        } catch (Exception e) {
            logger.error("Cannot save forecast: cannot write resource " + res, e);
            throw new ConverterException(e);
        }
    }

}