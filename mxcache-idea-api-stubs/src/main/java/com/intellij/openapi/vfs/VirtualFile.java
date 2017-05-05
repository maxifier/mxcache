/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.openapi.vfs;

import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.UserDataHolderBase;
import java.io.IOException;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class VirtualFile extends UserDataHolderBase implements ModificationTracker {
     public String toString() {
          return null;
     }

     public abstract byte[] contentsToByteArray() throws IOException;

     public String getUrl() {
          return null;
     }

     public long getModificationStamp() {
          return 0L;
     }

     public VirtualFile createChildDirectory(Object p1, String p2) throws IOException {
          return null;
     }

     public String getExtension() {
          return null;
     }

     public abstract boolean isDirectory();

     public final void setBinaryContent(byte[] p1) throws IOException {}

     public VirtualFile findChild(String p1) {
          return null;
     }

     public abstract VirtualFile[] getChildren();

     public VirtualFile createChildData(Object p1, String p2) throws IOException {
          return null;
     }

}
