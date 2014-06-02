/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.openapi.compiler;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CompileContext extends UserDataHolder {
     VirtualFile getModuleOutputDirectoryForTests(Module p1);

     void addMessage(CompilerMessageCategory p1, String p2, String p3, int p4, int p5);

     CompileScope getCompileScope();

     VirtualFile getModuleOutputDirectory(Module p1);

}
