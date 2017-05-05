/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.util.Set;

/**
 * @author Artyom Norin (artem.norin@maxifier.com)
 */
public class MxCacheGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        addInstrumentationFinalizer(project, "compileJava");
        addInstrumentationFinalizer(project, "compileTestJava");
    }

    private void addInstrumentationFinalizer(Project project, String compileTaskName) {
        Task compileTask = project.getTasks().getByPath(compileTaskName);
        if (compileTask != null) {
            Set<File> files = compileTask.getOutputs().getFiles().getFiles();
            InstrumentationTask instrumentationTask = project.getTasks().create(compileTaskName + "Instrumentation", InstrumentationTask.class);
            instrumentationTask.setClassesRootFolders(files);
            compileTask.finalizedBy(instrumentationTask);
        }
    }
}
