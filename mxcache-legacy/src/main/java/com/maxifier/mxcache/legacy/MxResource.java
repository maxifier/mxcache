package com.maxifier.mxcache.legacy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Project: Smart Advertising
 * Created by: Yakoushin Andrey
 * Date: 24.12.2009
 * Time: 13:12:52
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public interface MxResource {
    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream(boolean append) throws IOException;

    boolean exists();

    boolean deleteOnExit();
}
