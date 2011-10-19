package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.legacy.MxResource;
import com.maxifier.mxcache.legacy.MxResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 10:59:56
 */
public class MxConvertBytesToResource extends MxAbstractResourceConverter<byte[]> {
    private static final Logger logger = LoggerFactory.getLogger(MxConvertBytesToResource.class);

    public MxConvertBytesToResource(MxResourceManager resourceManager) {
        super(resourceManager);
    }

    public MxConvertBytesToResource(MxResourceManager resourceManager, String resourceNameTemplate) {
        super(resourceManager, resourceNameTemplate);
    }

    @Override
    public MxResource convert(byte[] t) {
        if (t == null) {
            return null;
        }
        MxResource res = createUniqueCacheFile(t.getClass());
        try {
            OutputStream fos = res.getOutputStream(false);
            try {
                fos.write(t);
            } finally {
                fos.close();
            }
            return res;
        } catch (IOException e) {
            logger.error("Cannot save forecast: cannot write file " + res, e);
            throw new ConverterException(e);
        }
    }
}
