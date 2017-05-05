/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.openapi.compiler;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface FileProcessingCompiler extends Compiler, ValidityStateFactory {
     FileProcessingCompiler.ProcessingItem[] getProcessingItems(CompileContext p1);

     FileProcessingCompiler.ProcessingItem[] process(CompileContext p1, FileProcessingCompiler.ProcessingItem[] p2);

     public static interface ProcessingItem {
          ValidityState getValidityState();

          VirtualFile getFile();

     }
}
