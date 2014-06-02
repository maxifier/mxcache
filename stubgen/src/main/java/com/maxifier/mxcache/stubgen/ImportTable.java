/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

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
        add(new THashSet<Type>(), genericTypes);
    }

    private void add(Set<java.lang.reflect.Type> visited, java.lang.reflect.Type... genericTypes) {
        for (Type genericType : genericTypes) {
            add(visited, genericType);
        }
    }

    public void add(java.lang.reflect.Type genericType) {
        add(new THashSet<Type>(), genericType);
    }

    private void add(Set<Type> visited, Type genericType) {
        if (!visited.add(genericType)) {
            return;
        }
        if (genericType instanceof Class) {
            add((Class) genericType);
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType gt = (GenericArrayType)genericType;
            add(visited, gt.getGenericComponentType());
        } else if (genericType instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable)genericType;
            add(visited, tv.getBounds());
        } else if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)genericType;
            add(visited, pt.getRawType());
            add(visited, pt.getActualTypeArguments());
        } else if (genericType instanceof WildcardType) {
            WildcardType wt = (WildcardType)genericType;
            add(visited, wt.getUpperBounds());
            add(visited, wt.getLowerBounds());
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
            Class enclosingClass = importedClass.getEnclosingClass();
            while (enclosingClass != null) {
                importedClass = enclosingClass;
                enclosingClass = importedClass.getEnclosingClass();
            }
            if (!importedClass.getPackage().equals(currentPackage) && !importedClass.getCanonicalName().startsWith("java.lang.")) {
                sortedImports.add(importedClass.getCanonicalName());
            }
        }
        return sortedImports;
    }
}
