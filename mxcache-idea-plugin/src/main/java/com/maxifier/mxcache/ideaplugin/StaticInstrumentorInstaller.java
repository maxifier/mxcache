package com.maxifier.mxcache.ideaplugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 29.03.2010
 * Time: 11:33:11
 */
public class StaticInstrumentorInstaller implements ProjectComponent, JDOMExternalizable {
    private final Project project;
    private final StaticInstrumentator instrumentator;

    public StaticInstrumentorInstaller(Project project) {
        this.project = project;
        instrumentator = new StaticInstrumentator();
    }

    public void projectOpened() {
        CompilerManager.getInstance(project).addCompiler(instrumentator);
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

    //==================================================================================================================

    public boolean isEnabled() {
        return instrumentator.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        instrumentator.setEnabled(enabled);
    }

    //==================================================================================================================

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        setEnabled(Boolean.valueOf(element.getChild("enabled").getText()));
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        Element e = new Element("enabled");
        e.setText(String.valueOf(isEnabled()));
        element.addContent(e);
    }
}
