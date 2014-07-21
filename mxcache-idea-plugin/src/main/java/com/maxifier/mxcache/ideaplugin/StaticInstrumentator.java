/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import com.maxifier.mxcache.MxCacheException;
import com.maxifier.mxcache.instrumentation.ClassDefinition;
import com.maxifier.mxcache.instrumentation.ClassInstrumentationResult;
import com.maxifier.mxcache.instrumentation.Instrumentator;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.lang.reflect.InvocationTargetException;

import com.intellij.openapi.compiler.*;
import com.intellij.openapi.vfs.VirtualFile;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;

import javax.swing.*;

/**
 * <p>
 * <b>Note:</b> when importing this module into Idea from maven, you may need to manually set type="PLUGIN_MODULE" in module
 * .iml file to be able to debug it in Idea and set <code>META-INF/plugin.xml</code> location to
 * <code>mxcache-ideaplugin/src/main/resources/</code>
 * </p><p>
 * You also need to manually remove maven's version of openide and plug the one from your idea distrubution.
 * </p><p>
 * This class may be application component but idea 7 doesn't support compiler extension point, so a workaround is
 * used to install it.
 * </p>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class StaticInstrumentator implements ClassInstrumentingCompiler /*, ApplicationComponent */ {
    private final InstrumentatorFinder instrumentatorFinder;

    private boolean enabled = true;

    public StaticInstrumentator(InstrumentatorFinder instrumentatorFinder) {
        this.instrumentatorFinder = instrumentatorFinder;
    }


    @Override
    @Nonnull
    public ProcessingItem[] getProcessingItems(CompileContext compileContext) {
        if (!enabled) {
            return new ProcessingItem[] {};
        }

        List<CachedProcessingItem> items = new ArrayList<CachedProcessingItem>();

        for (Module module : compileContext.getCompileScope().getAffectedModules()) {
            Instrumentator instrumentator = instrumentatorFinder.getInstrumentator(module);
            if (instrumentator != null) {
                VirtualFile outputDirectory = compileContext.getModuleOutputDirectory(module);
                if (outputDirectory != null) {
                    findAllClasses(items, instrumentator, outputDirectory, outputDirectory);
                }
                VirtualFile testDirectory = compileContext.getModuleOutputDirectoryForTests(module);
                if (testDirectory != null) {
                    findAllClasses(items, instrumentator, testDirectory, testDirectory);
                }
            }
        }

        return items.toArray(new ProcessingItem[items.size()]);
    }

    @Override
    public ProcessingItem[] process(CompileContext compileContext, ProcessingItem[] processingItems) {
        List<ProcessingItem> res = new ArrayList<ProcessingItem>(processingItems.length);
        for (ProcessingItem item : processingItems) {
            CachedProcessingItem simpleProcessingItem = (CachedProcessingItem) item;
            VirtualFile file = item.getFile();
            try {
                instrument(simpleProcessingItem);
                res.add(item);
            } catch (Exception e) {
                ApplicationManager.getApplication().runReadAction(new ErrorNotificationAction(compileContext, e, file));
            }
        }
        return res.toArray(new ProcessingItem[res.size()]);
    }

    private byte[] readFile(VirtualFile file) throws IOException {
        return ApplicationManager.getApplication().runReadAction(new FileReaderAction(file));
    }

    private void instrument(CachedProcessingItem item) throws IOException, InvocationTargetException, InterruptedException {
        byte[] bytecode = readFile(item.getFile());
        ClassInstrumentationResult result = item.getInstrumentator().instrument(bytecode);
        if (result != null) {
            InstrumentationAction action = new InstrumentationAction(result, item);
            SwingUtilities.invokeAndWait(new SwingWriteAction(action));
        }
    }

    @Override
    @Nonnull
    public String getDescription() {
        return "MxCache instrumentator";
    }

    @Override
    public boolean validateConfiguration(CompileScope compileScope) {
        return true;
    }

    /// theese two methods are used in different versions of intellij idea openapi /////////////////////////////////////
    /// be careful, don't put @Override annotations here - it may break compilation ////////////////////////////////////

    // this method is used in Idea 7 and earlier
    @SuppressWarnings({ "override" })
    public ValidityState createValidityState(DataInputStream dataInput) throws IOException {
        return createValidityState((DataInput) dataInput);
    }

    // this method is used in Idea 8 and newer
    @SuppressWarnings({ "override" })
    public ValidityState createValidityState(DataInput dataInput) throws IOException {
        return new CachedValidityState(dataInput);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void findAllClasses(List<CachedProcessingItem> list, Instrumentator mxcacheVersion, VirtualFile outputDir, VirtualFile rootDir) {
        assert rootDir.isDirectory() : "rootDir isn't a directory";

        for (VirtualFile entry : rootDir.getChildren()) {
            if (entry.isDirectory()) {
                findAllClasses(list, mxcacheVersion, outputDir, entry);
            } else if ("class".equals(entry.getExtension())) {
                list.add(new CachedProcessingItem(outputDir, entry, mxcacheVersion));
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private static class InstrumentationAction implements Runnable {
        private final ClassInstrumentationResult result;
        private final CachedProcessingItem item;

        public InstrumentationAction(ClassInstrumentationResult result, CachedProcessingItem item) {
            this.result = result;
            this.item = item;
        }

        @Override
        public void run() {
            List<ClassDefinition> additionalClasses = result.getAdditionalClasses();
            try {
                for (ClassDefinition additionalClass : additionalClasses) {
                    try {
                        VirtualFile dir = item.getOutputDirectory();
                        String[] path = additionalClass.getName().split("\\.");
                        for (int i = 0; i<path.length-1; i++) {
                            dir = getOrCreateDirectory(dir, path[i]);
                        }

                        String classFileName = path[path.length-1] + ".class";
                        VirtualFile file = getOrCreateFile(dir, classFileName);

                        file.setBinaryContent(additionalClass.getBytecode());
                    } catch (IOException e) {
                        throw new MxCacheException(e);
                    }
                }
                item.getFile().setBinaryContent(result.getInstrumentedBytecode());
            } catch (IOException e) {
                throw new MxCacheException(e);
            }
        }

        private VirtualFile getOrCreateFile(VirtualFile dir, String fileName) throws IOException {
            VirtualFile file = dir.findChild(fileName);
            if (file != null) {
                return file;
            }
            return dir.createChildData(this, fileName);
        }

        private VirtualFile getOrCreateDirectory(VirtualFile dir, String fileName) throws IOException {
            VirtualFile file = dir.findChild(fileName);
            if (file != null) {
                return file;
            }
            return dir.createChildDirectory(this, fileName);
        }
    }

    /** It's unused since a workaround is used for idea 7 */
    /*
    //////////// ApplicationComponent methods //////////////////////////////////////////////////////////////////////////

    @Nonnull
    public String getComponentName() {
        return "MxCache instrumentator";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    */

    private static class ErrorNotificationAction implements Runnable {
        private final CompileContext compileContext;
        private final Exception e;
        private final VirtualFile file;

        public ErrorNotificationAction(CompileContext compileContext, Exception e, VirtualFile file) {
            this.compileContext = compileContext;
            this.e = e;
            this.file = file;
        }

        @Override
        public void run() {
            compileContext.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), file.getUrl(), -1, -1);
        }
    }

    private static class FileReaderAction implements Computable<byte[]> {
        private final VirtualFile file;

        public FileReaderAction(VirtualFile file) {
            this.file = file;
        }

        @Override
        public byte[] compute() {
            try {
                return file.contentsToByteArray();
            } catch (IOException e) {
                throw new MxCacheException(e);
            }
        }
    }
}
