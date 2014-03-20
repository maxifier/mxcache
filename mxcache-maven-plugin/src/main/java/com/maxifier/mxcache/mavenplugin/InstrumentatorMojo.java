/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.mavenplugin;

import com.maxifier.mxcache.PublicAPI;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com):5
 * <p/>
 *
 * @goal instrument
 * @phase process-classes
 * 
 */
@SuppressWarnings ({ "JavaDoc" })
@PublicAPI
public class InstrumentatorMojo extends AbstractInstrumentatorMojo {
    /**
     * The directory for compiled classes.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    private File outputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        instrumentClasses(outputDirectory);
    }

}
