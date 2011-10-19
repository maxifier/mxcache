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
 * @goal instrument-tests
 * @phase process-test-classes
 */
@SuppressWarnings ({ "JavaDoc" })
public class TestInstrumentatorMojo extends AbstractInstrumentatorMojo {
    /**
     * The directory for compiled classes.
     *
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    private File testOutputDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        instrumentClasses(testOutputDirectory);
    }

}