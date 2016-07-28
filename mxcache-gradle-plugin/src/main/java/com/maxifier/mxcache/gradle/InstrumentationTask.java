/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.gradle;

import com.maxifier.mxcache.instrumentation.ClassfileInstrumentator;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Set;

/**
 * @author Artyom Norin (artem.norin@maxifier.com)
 */
public class InstrumentationTask extends AbstractTask {

    private Set<File> classesRootFolders;


    public void setClassesRootFolders(Set<File> classesRootFolders) {
        this.classesRootFolders = classesRootFolders;
    }

    @TaskAction
    public void performClassfilesInstrumentation() {
        try {
            for (File file : classesRootFolders) {
                String result = ClassfileInstrumentator.instrumentClasses(file);
                getLogger().info(result);
            }
        } catch (Exception e) {
            getLogger().error("MxCache instrumentation failed", e);
        }
    }
}
