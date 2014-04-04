/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.codeInspection;

import com.intellij.psi.PsiElement;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CustomSuppressableInspectionTool {
     public abstract SuppressIntentionAction[] getSuppressActions(PsiElement p1);

     public abstract boolean isSuppressedFor(PsiElement p1);

}
