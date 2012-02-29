package com.maxifier.mxcache.ideaplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.search.GlobalSearchScope;
import com.maxifier.mxcache.MxCacheException;
import com.maxifier.mxcache.instrumentation.ClassInstrumentationResult;
import com.maxifier.mxcache.instrumentation.current.InstrumentatorImpl;

import java.lang.reflect.InvocationTargetException;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 09.08.2010
* Time: 12:04:02
*/
enum MxCacheVersion {
    V219(InstrumentatorImpl.INSTANCE_219),
    V229(InstrumentatorImpl.INSTANCE_229),
    V2228(InstrumentatorImpl.INSTANCE_2228);

    public ClassInstrumentationResult instrument(byte[] bytecode) {
        return instrumentator.instrument(bytecode);
    }

    private final com.maxifier.mxcache.instrumentation.Instrumentator instrumentator;

    MxCacheVersion(com.maxifier.mxcache.instrumentation.Instrumentator instrumentator) {
        this.instrumentator = instrumentator;
    }

    public static MxCacheVersion of(final Module module) {
        VersionFinderAction finder = new VersionFinderAction(module);
        ApplicationManager.getApplication().runReadAction(finder);
        return finder.getVersion();
    }

    private static class VersionFinderAction implements Runnable {
        private final Module module;

        private MxCacheVersion version;

        public VersionFinderAction(Module module) {
            this.module = module;
        }

        public MxCacheVersion getVersion() {
            return version;
        }

        @Override
        public void run() {
            version = findVersion();
        }

        private MxCacheVersion findVersion() {
            PsiClass cls = findGlobalClass("com.maxifier.mxcache.MxCache");
            if (cls == null) {
                return null;
            }
            String versionIdentifier = getVersionField(cls);
            if (versionIdentifier.equals("2.1.9")) {
                return V219;
            }
            if (versionIdentifier.equals("2.2.9")) {
                return V229;
            }
            if (versionIdentifier.equals("2.2.28")) {
                return V2228;
            }
            throw new IllegalStateException("Unsupported version of MxCache: " + versionIdentifier + ", update MxCache Idea plugin or choose another version of MxCache");
        }

        private String getVersionField(PsiClass old) {
            String versionIdentifier = null;
            for (PsiField psiField : old.getFields()) {
                if (psiField.getName().equals("VERSION")) {
                    versionIdentifier = (String) ((PsiLiteralExpression) psiField.getInitializer()).getValue();
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

    private static final class IdeaVersionException extends MxCacheException {
        private IdeaVersionException(Throwable cause) {
            super(cause);
        }
    }
}
