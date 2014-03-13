/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.asm.Type;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class CalculatableHelper {
    private static final Pattern NAME_PATTERN = Pattern.compile("^((\\p{javaJavaIdentifierPart}+\\.)*\\p{javaJavaIdentifierPart}+)\\$Calculable\\$\\p{javaJavaIdentifierPart}+\\$(\\d+)$");

    private CalculatableHelper() {
    }

    public static String getCalculatableName(Type ownerType, String methodName, int id) {
        return ownerType.getInternalName() + "$Calculable$" + methodName + "$" + id;
    }

    public static CacheId getId(Class cls) {
        String name = cls.getName();
        Matcher matcher = NAME_PATTERN.matcher(name);
        if (matcher.find()) {
            String className = matcher.group(1);
            String idString = matcher.group(3);
            Class<?> ownerClass;
            try {
                ownerClass = cls.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
            int id = Integer.parseInt(idString);
            return new CacheId(ownerClass, id);
        }
        return null;
    }
}
