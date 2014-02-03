package com.maxifier.mxcache.ideaplugin.inspections;

import javax.annotation.Nonnull;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.components.ApplicationComponent;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.03.2010
 * Time: 11:21:19
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
