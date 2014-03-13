/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.maxifier.mxcache.ideaplugin.MxCacheInspection;
import com.maxifier.mxcache.transform.ReversibleTransform;
import com.maxifier.mxcache.transform.Transform;

import javax.annotation.Nonnull;

import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CacheAccessorsInspection extends MxCacheInspection {
    private static final String INSPECTION_ID = "MxCacheCachedAccessors";

    @Nonnull
    @Override
    public String getDisplayName() {
        return "MxCache @Cached problems";
    }

    @Nonnull
    @Override
    public String getShortName() {
        return INSPECTION_ID;
    }

    @Override
    protected void checkMethod(PsiMethod method, InspectionManager inspectionManager, List<ProblemDescriptor> res) {
        boolean useProxy = false;
        PsiAnnotation cached = null;
        PsiModifierList modifiers = method.getModifierList();
        for (PsiAnnotation annotation : modifiers.getAnnotations()) {
            PsiJavaCodeReferenceElement ref = annotation.getNameReferenceElement();
            if (ref != null) {
                String qualifiedName = ref.getQualifiedName();
                if (isUseProxy(qualifiedName)) {
                    useProxy = true;
                } else if (isCached(qualifiedName)) {
                    cached = annotation;
                }
            }
        }
        if (cached != null) {
            checkCached(method, inspectionManager, res, modifiers, cached, useProxy);
        } else if (useProxy) {
            checkParams(method, inspectionManager, res, true);
        } else {
            checkNonCachedNonProxy(method, inspectionManager, res);
        }
    }

    private void checkNonCachedNonProxy(PsiMethod method, InspectionManager inspectionManager, List<ProblemDescriptor> res) {
        for (PsiParameter param : method.getParameterList().getParameters()) {
            for (PsiAnnotation annotation : param.getModifierList().getAnnotations()) {
                PsiJavaCodeReferenceElement ref = annotation.getNameReferenceElement();
                if (ref != null) {
                    String qualifiedName = ref.getQualifiedName();
                    if (isTransform(qualifiedName) || isReversibleTransform(qualifiedName)) {
                        reportError(inspectionManager, res, annotation, "Only cached methods or proxied methods may have @Transform or @ReversibleTransform annotations");
                    }
                }
            }
        }
    }

    private void checkCached(PsiMethod method, InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiModifierList modifiers, PsiAnnotation cached, boolean useProxy) {
        PsiType returnType = method.getReturnType();
        if (returnType == null) {
            reportError(inspectionManager, res, cached, "@Cached method should return a value");
        } else {
            String returnTypeName = returnType.getCanonicalText();
            if (returnTypeName.equals("void")) {
                reportError(inspectionManager, res, cached, "@Cached method should not return void");
            }
        }
        if (method.getContainingClass().isInterface()) {
            reportError(inspectionManager, res, cached, "Interface should not have @Cached methods");
        } else {
            if (modifiers.hasModifierProperty("abstract")) {
                reportError(inspectionManager, res, cached, "@Cached method should not be abstract");
            }
            if (modifiers.hasModifierProperty("native")) {
                reportError(inspectionManager, res, cached, "@Cached method should not be native");
            }
        }

        checkParams(method, inspectionManager, res, useProxy);
    }

    private void checkParams(PsiMethod method, InspectionManager inspectionManager, List<ProblemDescriptor> res, boolean useProxy) {
        for (PsiParameter param : method.getParameterList().getParameters()) {
            boolean transform = false;
            for (PsiAnnotation annotation : param.getModifierList().getAnnotations()) {
                PsiJavaCodeReferenceElement ref = annotation.getNameReferenceElement();
                if (ref != null) {
                    String qualifiedName = ref.getQualifiedName();
                    if (useProxy && isTransform(qualifiedName)) {
                        reportError(inspectionManager, res, annotation, "Proxied method should have @ReversibleTransform annotation instead of @Transform");
                    }
                    if (isTransform(qualifiedName) || isReversibleTransform(qualifiedName)) {
                        if (transform) {
                            reportError(inspectionManager, res, annotation, "Duplicate transformation annotation");
                        } else {
                            transform = true;
                        }
                    }
                }
            }
        }
    }

    private boolean isTransform(String qualifiedName) {
        return qualifiedName != null && qualifiedName.equals(Transform.class.getCanonicalName());
    }

    private boolean isReversibleTransform(String qualifiedName) {
        return qualifiedName != null && qualifiedName.equals(ReversibleTransform.class.getCanonicalName());
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }
}
