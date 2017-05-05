/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.mavenplugin;

import com.maxifier.mxcache.instrumentation.ClassfileInstrumentator;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AbstractInstrumentatorMojo extends AbstractInstrumentator {
    protected void instrumentClasses(File path) throws MojoExecutionException {
        try {
            String resultLog = ClassfileInstrumentator.instrumentClasses(path);
            getLog().info(resultLog);
        } catch (Exception e) {
            MojoExecutionException exception = new MojoExecutionException(e.getMessage(), e.getCause());
            getLog().error(exception);
            throw exception;
        }
    }
}
