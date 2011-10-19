package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.legacy.MxResource;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 10:59:56
 */
public class MxConvertResourceToBytes implements MxConverter<MxResource, byte[]> {
    private static final Logger logger = LoggerFactory.getLogger(MxConvertResourceToBytes.class);

    @Override
    public byte[] convert(MxResource res) {
        if (res == null) {
            return null;
        }
        if (!res.exists()) {
            logger.error("Resource doesn't exist " + res);
            return null;
        }
        try {
            InputStream in = res.getInputStream();
            try {
                return IOUtils.toByteArray(in);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } catch (IOException e) {
            throw new ConverterException(e);
        }
    }
}