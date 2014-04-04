/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.openapi.options;

import javax.swing.JComponent;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface UnnamedConfigurable {
     public abstract void apply() throws ConfigurationException;

     public abstract void disposeUIResources();

     public abstract JComponent createComponent();

     public abstract boolean isModified();

     public abstract void reset();

}
