package com.maxifier.mxcache.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.03.2010
 * Time: 15:04:5
 * <p/>
 *
 * @goal instrument
 * @phase process-classes
 * 
 */
@SuppressWarnings ({ "JavaDoc" })
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
