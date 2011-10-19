package com.maxifier.mxcache.legacy;

/**
 * Project: Smart Advertising
 * Created by: Yakoushin Andrey
 * Date: 24.12.2009
 * Time: 13:25:00
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public interface MBeanRegistrator {
    void registerMBean(String s, Object o) throws Exception;
}
