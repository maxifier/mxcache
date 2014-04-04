/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
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
     public VirtualFile() {}
     public String toString() {
          throw new UnsupportedOperationException();
     }

     public abstract byte[] contentsToByteArray() throws IOException;

     public String getUrl() {
          throw new UnsupportedOperationException();
     }

     public long getModificationStamp() {
          throw new UnsupportedOperationException();
     }

     public VirtualFile createChildDirectory(Object p1, String p2) throws IOException {
          throw new UnsupportedOperationException();
     }

     public String getExtension() {
          throw new UnsupportedOperationException();
     }

     public abstract boolean isDirectory();

     public final void setBinaryContent(byte[] p1) throws IOException {
          throw new UnsupportedOperationException();
     }

     public VirtualFile findChild(String p1) {
          throw new UnsupportedOperationException();
     }

     public abstract VirtualFile[] getChildren();

     public VirtualFile createChildData(Object p1, String p2) throws IOException {
          throw new UnsupportedOperationException();
     }

}
