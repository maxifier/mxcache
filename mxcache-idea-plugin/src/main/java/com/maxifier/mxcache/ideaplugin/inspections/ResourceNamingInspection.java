package com.maxifier.mxcache.ideaplugin.inspections;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.maxifier.mxcache.ideaplugin.MxCacheInspection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
* User: dalex
* Date: 17.03.2010
* Time: 11:39:36
*/
public class ResourceNamingInspection extends MxCacheInspection {
    private static final String INSPECTION_ID = "MxCacheResourceNaming";

    private static final Pattern NAME_PATTERN = Pattern.compile("([a-zA-Z_][\\w_]*\\.)*([a-zA-Z_][\\w_]*)");
    private static final int MIN_NAME_LENGTH = 4;

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "MxCache resource naming problems";
    }

    @NotNull
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
                if (isResourceAccessor(qualifiedName) || isExplicitDependency(qualifiedName)) {
                    checkAttributes(inspectionManager, res, annotation);
                }
            }
        }
    }

    private void checkAttributes(InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiAnnotation annotation) {
        for (PsiNameValuePair pair : annotation.getParameterList().getAttributes()) {
            String name = pair.getName();
            if (name == null || "value".equals(name)) {
                checkResourceNames(inspectionManager, res, pair);
            }
        }
    }

    private void checkResourceNames(InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiNameValuePair pair) {
        PsiAnnotationMemberValue value = pair.getValue();
        if (value instanceof PsiArrayInitializerMemberValue) {
            PsiArrayInitializerMemberValue array = (PsiArrayInitializerMemberValue) value;
            for (PsiAnnotationMemberValue v : array.getInitializers()) {
                checkResourceName(inspectionManager, res, v);
            }
        } else {
            // otherwise it can be literal
            checkResourceName(inspectionManager, res, value);
        }
    }

    private void checkResourceName(InspectionManager inspectionManager, List<ProblemDescriptor> res, PsiAnnotationMemberValue value) {
        if (value instanceof PsiLiteralExpression) {
            Object v = ((PsiLiteralExpression) value).getValue();
            if (v instanceof String) {
                String tag = (String) v;
                if (tag.isEmpty()) {
                    reportError(inspectionManager, res, value, "Resource name should not be empty");
                } else {
                    if (!isValidGroupOrTagName(tag)) {
                        reportError(inspectionManager, res, value, "Resource name should be valid Java name");
                    }
                    if (tag.length() < MIN_NAME_LENGTH) {
                        reportError(inspectionManager, res, value, "Resource name is too short");
                    }
                }
            }
        }
    }

    private boolean isValidGroupOrTagName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }
}