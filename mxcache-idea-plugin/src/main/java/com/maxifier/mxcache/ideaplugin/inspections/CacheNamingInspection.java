/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.maxifier.mxcache.ideaplugin.MxCacheInspection;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class CacheNamingInspection extends MxCacheInspection {
    private static final String INSPECTION_ID = "MxCacheNaming";

    private static final Pattern NAME_PATTERN = Pattern.compile("([a-zA-Z_][\\w_]*\\.)*([a-zA-Z_][\\w_]*)");
    private static final int MIN_NAME_LENGTH = 4;

    @Nonnull
    @Override
    public String getDisplayName() {
        return "MxCache naming problems";
    }

    @Nonnull
    @Override
    public String getShortName() {
        return INSPECTION_ID;
    }

    @Override
    protected void checkMethod(PsiMethod method, InspectionManager inspectionManager, List<ProblemDescriptor> res) {
        PsiModifierList modifiers = method.getModifierList();
        for (PsiAnnotation annotation : modifiers.getAnnotations()) {
            PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
            if (referenceElement != null) {
                String qualifiedName = referenceElement.getQualifiedName();
                if (isCached(qualifiedName)) {
                    checkAttributes(inspectionManager, res, annotation);
                    return;
                }
            }
        }
    }

    private void checkAttributes(InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiAnnotation annotation) {
        for (PsiNameValuePair pair : annotation.getParameterList().getAttributes()) {
            String name = pair.getName();
            if ("group".equals(name)) {
                checkGroupName(inspectionManager, res, pair);
            } else if ("tags".equals(name)) {
                checkTagNames(inspectionManager, res, pair);
            }
        }
    }

    private void checkTagNames(InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiNameValuePair pair) {
        PsiAnnotationMemberValue value = pair.getValue();
        if (value instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue array = (PsiArrayInitializerMemberValue) value;
            for (PsiAnnotationMemberValue v : array.getInitializers()) {
                checkTagName(inspectionManager, res, v);
            }
        } else {
            // otherwise it can be literal
            checkTagName(inspectionManager, res, value);
        }
    }

    private void checkTagName(InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiAnnotationMemberValue value) {
        if (value instanceof PsiLiteralExpression) {
            Object v = ((PsiLiteralExpression) value).getValue();
            if (v instanceof String) {
                String tag = (String)v;
                if (tag.isEmpty()) {
                    reportError(inspectionManager, res, value, "Tag name should not be empty");
                } else {
                    if (!isValidGroupOrTagName(tag)) {
                        reportError(inspectionManager, res, value, "Tag name should be valid Java name");
                    }
                    if (tag.length() < MIN_NAME_LENGTH) {
                        reportError(inspectionManager, res, value, "Tag name is too short");
                    }
                }
            }
        }
    }

    private void checkGroupName(InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiNameValuePair pair) {
        PsiAnnotationMemberValue value = pair.getValue();
        if (value instanceof PsiLiteralExpression) {
            Object v = ((PsiLiteralExpression) value).getValue();
            if (v instanceof String) {
                String group = (String) v;
                if (group.isEmpty()) {
                    reportError(inspectionManager, res, value, "Group name should not be empty");
                } else {
                    if (!isValidGroupOrTagName(group)) {
                        reportError(inspectionManager, res, value, "Group name should be valid Java name");
                    }
                    if (group.length() < MIN_NAME_LENGTH) {
                        reportError(inspectionManager, res, value, "Group name is too short");
                    }
                }
            }
        }
    }

    private boolean isValidGroupOrTagName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }
}