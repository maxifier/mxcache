/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.codeInspection;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class SuppressManager implements BatchSuppressManager {
     public static SuppressManager getInstance() {
          throw new UnsupportedOperationException();
     }

     public abstract SuppressIntentionAction[] createSuppressActions(HighlightDisplayKey p1);

}
