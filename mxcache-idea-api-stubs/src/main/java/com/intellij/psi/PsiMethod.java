/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.intellij.psi;

import com.intellij.pom.PomRenameableTarget;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface PsiMethod extends PsiMember, PsiNameIdentifierOwner, PsiModifierListOwner, PsiDocCommentOwner, PsiTypeParameterListOwner, PomRenameableTarget<PsiElement>, PsiTarget {
     PsiModifierList getModifierList();

     PsiParameterList getParameterList();

     PsiType getReturnType();

}
