/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.jpsplugin;

import org.jetbrains.jps.incremental.BuilderService;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * MxCacheBuildServiceInstrumentator
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-03-14 17:27)
 */
public class MxCacheBuildService extends BuilderService {
    @Nonnull
    @Override
    public List<? extends ModuleLevelBuilder> createModuleLevelBuilders() {
        return Arrays.asList(new MxCacheBuildInstrumentator());
    }
}
