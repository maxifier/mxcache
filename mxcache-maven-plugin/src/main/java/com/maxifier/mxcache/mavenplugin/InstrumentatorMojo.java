/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.mavenplugin;

import com.maxifier.mxcache.PublicAPI;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Performs mxcache instrumentation of classes.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings ({ "JavaDoc" })
@PublicAPI
@Mojo(name = "instrument", defaultPhase = LifecyclePhase.PROCESS_CLASSES, threadSafe = true)
public class InstrumentatorMojo extends AbstractInstrumentatorMojo {
    /**
     * The directory for compiled classes.
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    @Parameter(name = "outputDirectory", defaultValue = "${project.build.outputDirectory}", required = true, readonly = true)
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        instrumentClasses(outputDirectory);
    }

}
