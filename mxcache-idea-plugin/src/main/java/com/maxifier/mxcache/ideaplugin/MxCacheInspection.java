package com.maxifier.mxcache.ideaplugin;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.proxy.UseProxy;
import com.maxifier.mxcache.resource.ResourceDependency;
import com.maxifier.mxcache.resource.ResourceReader;
import com.maxifier.mxcache.resource.ResourceWriter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 09.04.2010
 * Time: 17:35:28
 */
public abstract class MxCacheInspection extends LocalInspectionTool implements CustomSuppressableInspectionTool {
    private static final String CACHED_NAME = Cached.class.getCanonicalName();
    private static final String USE_PROXY_NAME = UseProxy.class.getCanonicalName();
    private static final String RESOURCE_DEPENDENCY_NAME = ResourceDependency.class.getCanonicalName();
    private static final String RESOURCE_WRITER_NAME = ResourceWriter.class.getCanonicalName();
    private static final String RESOURCE_READER_NAME = ResourceReader.class.getCanonicalName();

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "MxCache problems";
    }

    @NotNull
    @Override
    // remember: there is no pattern validation in idea 7!
    public String getID() {
        return getShortName();
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    protected void reportError(InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiElement element, String s) {
        reportError(inspectionManager, res, element, s, null);
    }

    protected void reportError(InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiElement element, String s, LocalQuickFix fix) {
        res.add(inspectionManager.createProblemDescriptor(element, s, fix, ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
    }

    public boolean isCached(String qualifiedName) {
        return qualifiedName != null && qualifiedName.equals(CACHED_NAME);
    }

    public boolean isUseProxy(String qualifiedName) {
        return qualifiedName != null && qualifiedName.equals(USE_PROXY_NAME);
    }

    public boolean isExplicitDependency(String qualifiedName) {
        return qualifiedName != null && qualifiedName.equals(RESOURCE_DEPENDENCY_NAME);
    }

    public boolean isResourceAccessor(String qualifiedName) {
        return qualifiedName != null && (isResourceReader(qualifiedName) || isResourceWriter(qualifiedName));
    }

    private boolean isResourceWriter(String qualifiedName) {
        return qualifiedName.equals(RESOURCE_WRITER_NAME);
    }

    private boolean isResourceReader(String qualifiedName) {
        return qualifiedName.equals(RESOURCE_READER_NAME);
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile psiFile, @NotNull InspectionManager inspectionManager, boolean b) {
        PsiElement[] methods = PsiTreeUtil.collectElements(psiFile, new PsiMethodFinder());
        List<ProblemDescriptor> res = new ArrayList<ProblemDescriptor>();
        for (PsiElement method : methods) {
            checkMethod((PsiMethod) method, inspectionManager, res);
        }
        return res.toArray(new ProblemDescriptor[res.size()]);
    }

    protected abstract void checkMethod(PsiMethod method, InspectionManager inspectionManager, List<ProblemDescriptor> res);

    public SuppressIntentionAction[] getSuppressActions(PsiElement element) {
        return SuppressManager.getInstance().createSuppressActions(HighlightDisplayKey.find(getID()));
    }

    public boolean isSuppressedFor(PsiElement element) {
        return SuppressManager.getInstance().isSuppressedFor(element, getID());
    }
}
