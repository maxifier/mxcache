/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin.inspections;

import javax.annotation.Nonnull;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.components.ApplicationComponent;

/**
* @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class Validator implements ApplicationComponent, InspectionToolProvider {

    @Nonnull
    public String getComponentName() {
        return "MxCache inspection";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Class[] getInspectionClasses() {
        return new Class[] { CacheAccessorsInspection.class, CacheNamingInspection.class,
                ResourceNamingInspection.class, ResourceCompatibilityInspection.class};
    }

}
