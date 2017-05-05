/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
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
          return null;
     }

     public abstract SuppressIntentionAction[] createSuppressActions(HighlightDisplayKey p1);

}
