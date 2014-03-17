/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

import javax.annotation.Nonnull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxCacheConfiguration implements ProjectComponent, Configurable {
    private final StaticInstrumentorInstaller installer;
    private JCheckBox enabledCheckbox;

    public MxCacheConfiguration(StaticInstrumentorInstaller installer) {
        this.installer = installer;
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @Nonnull
    @Override
    public String getComponentName() {
        return "MxCacheSettings";
    }

    @Override
    public String getDisplayName() {
        return "MxCache";
    }

    // In IDEA 13 there's no such method, so there's no override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public JComponent createComponent() {
        JPanel p = new JPanel(new BorderLayout());
        enabledCheckbox = new JCheckBox("Enable instrumentation");
        reset();
        p.add(enabledCheckbox, BorderLayout.NORTH);
        return p;
    }

    @Override
    public boolean isModified() {
        return enabledCheckbox.isSelected() != installer.isEnabled();
    }

    @Override
    public void apply() throws ConfigurationException {
        installer.setEnabled(enabledCheckbox.isSelected());
    }

    @Override
    public void reset() {
        enabledCheckbox.setSelected(installer.isEnabled());
    }

    @Override
    public void disposeUIResources() {
        enabledCheckbox = null;
    }
}
