/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.openapi.options;

import javax.swing.JComponent;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface UnnamedConfigurable {
     void apply() throws ConfigurationException;

     void disposeUIResources();

     JComponent createComponent();

     boolean isModified();

     void reset();

}
