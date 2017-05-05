/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

import javax.annotation.Nonnull;

/**
* @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class StaticInstrumentorInstaller implements ProjectComponent, JDOMExternalizable {
    private final Project project;
    private final StaticInstrumentator instrumentator;

    public StaticInstrumentorInstaller(Project project, InstrumentatorFinder instrumentatorFinder) {
        this.project = project;
        instrumentator = new StaticInstrumentator(instrumentatorFinder);
    }

    public void projectOpened() {
        CompilerManager.getInstance(project).addCompiler(instrumentator);
    }

    public void projectClosed() {
    }

    @Nonnull
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
