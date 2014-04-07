/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen;

import gnu.trove.THashMap;

import java.lang.reflect.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * ImportTable
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-07 11:19)
 */
class ImportTable {
    private final Package currentPackage;
    private final Map<String, Class> map;

    ImportTable(Package currentPackage) {
        this.currentPackage = currentPackage;
        map = new THashMap<String, Class>();
    }

    public void add(java.lang.reflect.Type... genericTypes) {
        for (Type genericType : genericTypes) {
            add(genericType);
        }
    }

    public void add(java.lang.reflect.Type genericType) {
        if (genericType instanceof Class) {
            add((Class) genericType);
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType gt = (GenericArrayType)genericType;
            add(gt.getGenericComponentType());
        } else if (genericType instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable)genericType;
            add(tv.getBounds());
        } else if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)genericType;
            add(pt.getRawType());
            add(pt.getActualTypeArguments());
        } else if (genericType instanceof WildcardType) {
            WildcardType wt = (WildcardType)genericType;
            add(wt.getUpperBounds());
            add(wt.getLowerBounds());
        }
    }

    public void add(Class... cs) {
        for (Class c : cs) {
            add(c);
        }
    }

    public void add(Class c) {
        c = resolveImportType(c);
        if (c != null) {
            map.put(getShortName(c), c);
        }
    }

    private String getShortName(Class c) {
        String packageName = c.getPackage().getName();
        String canonicalName = c.getCanonicalName();
        if (canonicalName.startsWith(packageName)) {
            return canonicalName.substring(packageName.length() + 1);
        }
        return c.getSimpleName();
    }

    public String getImportedName(Class c) {
        if (c.isArray()) {
            return getImportedName(c.getComponentType()) + "[]";
        }
        if (c.isPrimitive()) {
            return c.getCanonicalName();
        }
        String shortName = getShortName(c);
        if (map.get(shortName) == c) {
            return shortName;
        }
        return c.getCanonicalName();
    }

    private Class<?> resolveImportType(Class<?> t) {
        if (t == null) {
            return null;
        }
        while (t.isArray()) {
            t = t.getComponentType();
        }
        if (t.isPrimitive()) {
            return null;
        }
        return t;
    }

    public void forceSimpleName(Class c) {
        map.put(c.getSimpleName(), c);
    }

    public Set<String> getSortedImports() {
        Set<String> sortedImports = new TreeSet<String>();
        for (Class importedClass : map.values()) {
            if (!importedClass.getPackage().equals(currentPackage) && !importedClass.getCanonicalName().startsWith("java.lang.")) {
                sortedImports.add(importedClass.getCanonicalName());
            }
        }
        return sortedImports;
    }
}
