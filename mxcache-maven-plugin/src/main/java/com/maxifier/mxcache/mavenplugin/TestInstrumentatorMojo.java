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
 * Performs mxcache instrumentation of test classes.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings ({ "JavaDoc" })
@PublicAPI
@Mojo(name = "instrument-tests", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, threadSafe = true)
public class TestInstrumentatorMojo extends AbstractInstrumentatorMojo {
    /**
     * The directory for compiled classes.
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    @Parameter(name = "testOutputDirectory", defaultValue = "${project.build.testOutputDirectory}", required = true, readonly = true)
    private File testOutputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        instrumentClasses(testOutputDirectory);
    }

}