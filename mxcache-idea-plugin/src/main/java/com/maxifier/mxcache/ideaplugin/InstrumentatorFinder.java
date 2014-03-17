/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.search.GlobalSearchScope;
import com.maxifier.mxcache.MxCacheException;
import com.maxifier.mxcache.instrumentation.Instrumentator;
import com.maxifier.mxcache.instrumentation.InstrumentatorProvider;

import javax.annotation.Nonnull;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class InstrumentatorFinder implements ProjectComponent {
    private InstrumentatorFinder() { }

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
        return "InstrumentatorFinder";
    }

    public Instrumentator getInstrumentator(final Module module) {
        InstrumentatorFinderAction finder = new InstrumentatorFinderAction(module);
        ApplicationManager.getApplication().runReadAction(finder);
        return finder.getInstrumentator();
    }

    private class InstrumentatorFinderAction implements Runnable {
        private final Module module;

        private Instrumentator instrumentator;

        public InstrumentatorFinderAction(Module module) {
            this.module = module;
        }

        public Instrumentator getInstrumentator() {
            return instrumentator;
        }

        @Override
        public void run() {
            instrumentator = findInstrumentator();
        }

        private Instrumentator findInstrumentator() {
            PsiClass cls = findGlobalClass("com.maxifier.mxcache.MxCache");
            if (cls == null) {
                return null;
            }
            String versionIdentifier = getVersionField(cls);

            Instrumentator instrumentator = getVersion(versionIdentifier);
            if (instrumentator != null) {
                return instrumentator;
            }
            throw new IllegalStateException("Unsupported version of MxCache: " + versionIdentifier + ", update MxCache Idea plugin or choose another version of MxCache");
        }

        private String getVersionField(PsiClass old) {
            String versionIdentifier = null;
            for (PsiField psiField : old.getFields()) {
                if (psiField.getName().equals("VERSION")) {
                    PsiLiteralExpression initializer = (PsiLiteralExpression) psiField.getInitializer();
                    if (initializer != null) {
                        Object value = initializer.getValue();
                        if (value instanceof String) {
                            versionIdentifier = (String) value;
                        }
                    }
                }
            }
            return versionIdentifier;
        }

        private PsiClass findGlobalClass(String name) {
            GlobalSearchScope scope = module.getModuleWithDependenciesAndLibrariesScope(false);
            try {
                return findClass8(name, scope);
            } catch (IdeaVersionException e) {
                return findClass9(name, scope);
            }
        }

        @SuppressWarnings({ "unchecked" })
        private PsiClass findClass9(String name, GlobalSearchScope scope) {
            try {
                Class javaPsiFacadeClass = Class.forName("com.intellij.psi.JavaPsiFacade");
                Object facade = module.getProject().getComponent(javaPsiFacadeClass);
                return (PsiClass) javaPsiFacadeClass.getMethod("findClass", String.class, GlobalSearchScope.class).invoke(facade, name, scope);
            } catch (ClassNotFoundException e) {
                throw new IdeaVersionException(e);
            } catch (NoSuchMethodException e) {
                throw new IdeaVersionException(e);
            } catch (IllegalAccessException e) {
                throw new IdeaVersionException(e);
            } catch (InvocationTargetException e) {
                // that's not version exception!
                throw new MxCacheException(e);
            }
        }

        @SuppressWarnings({ "unchecked" })
        private PsiClass findClass8(String name, GlobalSearchScope scope) {
            try {
                Class psiManagerClass = Class.forName("com.intellij.psi.PsiManager");
                Object facade = module.getProject().getComponent(psiManagerClass);
                return (PsiClass) psiManagerClass.getMethod("findClass", String.class, GlobalSearchScope.class).invoke(facade, name, scope);
            } catch (ClassNotFoundException e) {
                throw new IdeaVersionException(e);
            } catch (NoSuchMethodException e) {
                throw new IdeaVersionException(e);
            } catch (IllegalAccessException e) {
                throw new IdeaVersionException(e);
            } catch (InvocationTargetException e) {
                // that's not version exception!
                throw new MxCacheException(e);
            }
        }
    }

    private Instrumentator getVersion(String version) {
        return InstrumentatorProvider.getAvailableVersions().get(version);
    }

    private static final class IdeaVersionException extends MxCacheException {
        private IdeaVersionException(Throwable cause) {
            super(cause);
        }
    }
}
