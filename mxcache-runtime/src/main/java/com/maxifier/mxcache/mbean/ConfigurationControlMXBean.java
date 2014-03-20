/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.mbean;

import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface ConfigurationControlMXBean {
    List<RuleInfo> getRules();
}
