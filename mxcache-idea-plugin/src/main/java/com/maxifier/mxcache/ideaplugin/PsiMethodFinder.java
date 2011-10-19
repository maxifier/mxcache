package com.maxifier.mxcache.ideaplugin;

import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

/**
 * Created by IntelliJ IDEA.
* User: dalex
* Date: 01.05.2010
* Time: 18:52:04
*/
class PsiMethodFinder implements PsiElementFilter {
    @Override
    public boolean isAccepted(PsiElement psiElement) {
        return psiElement instanceof PsiMethod;
    }
}
