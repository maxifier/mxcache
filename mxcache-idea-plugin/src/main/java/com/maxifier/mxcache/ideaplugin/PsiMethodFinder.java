/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class PsiMethodFinder implements PsiElementFilter {
    @Override
    public boolean isAccepted(PsiElement psiElement) {
        return psiElement instanceof PsiMethod;
    }
}
