package com.maxifier.mxcache.ideaplugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 29.03.2010
 * Time: 11:33:11
 */
public class StaticInstrumentorInstaller implements ProjectComponent {
    private final Project project;

    public StaticInstrumentorInstaller(Project project) {
        this.project = project;
    }

    public void projectOpened() {
        CompilerManager compilerManager = CompilerManager.getInstance(project);
        compilerManager.addCompiler(new StaticInstrumentator());
    }

    public void projectClosed() {
    }

    @NotNull
    public String getComponentName() {
        return "MxCache static instrumentator";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }
}
