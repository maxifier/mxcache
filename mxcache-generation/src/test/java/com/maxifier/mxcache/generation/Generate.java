/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.generation;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Generates classes that are created from templates. The "*.template" files must be in
 * the classpath (because Class.getResource() is used to load the files).
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class Generate {
    private static final Logger logger = LoggerFactory.getLogger(Generate.class);

    private static final String TEMPLATE_MODULE = "mxcache-generation";
    private static final String MODULE = "mxcache-runtime";

    private static final String TEMPLATE_PATH = TEMPLATE_MODULE + "/src/test/java/com/maxifier/mxcache/template";

    private static final WrapperInfo CHAR_INFO = new WrapperInfo("char", "Character", "'*'", "Short", "(short)$1", "(char)$1", false);
    private static final WrapperInfo DOUBLE_INFO = new WrapperInfo("double", "Double", "42d", false);
    private static final WrapperInfo FLOAT_INFO = new WrapperInfo("float", "Float", "42f", false);
    private static final WrapperInfo INT_INFO = new WrapperInfo("int", "Integer", "42", false);
    private static final WrapperInfo LONG_INFO = new WrapperInfo("long", "Long", "42L", false);
    private static final WrapperInfo BYTE_INFO = new WrapperInfo("byte", "Byte", "(byte)42", false);
    private static final WrapperInfo SHORT_INFO = new WrapperInfo("short", "Short", "(short)42", false);
    private static final WrapperInfo BOOL_INFO = new WrapperInfo("boolean", "Boolean", "true", "Byte", "(byte)($1? 1 : 0)", "$1 != 0", false);
    private static final WrapperInfo OBJECT_INFO = new WrapperInfo("Object", "Object", "\"123\"", true);
    private static final WrapperInfo NONE_INFO = new WrapperInfo("", "", "", false);

    private static final WrapperInfo[] WRAPPERS_NO_BOOLEAN = {
            CHAR_INFO,
            DOUBLE_INFO,
            FLOAT_INFO,
            INT_INFO,
            LONG_INFO,
            BYTE_INFO,
            SHORT_INFO
    };

    private static final WrapperInfo[] WRAPPERS_NO_BOOLEAN_OBJECT = {
            CHAR_INFO,
            DOUBLE_INFO,
            FLOAT_INFO,
            INT_INFO,
            LONG_INFO,
            BYTE_INFO,
            SHORT_INFO,
            OBJECT_INFO
    };

    private static final WrapperInfo[] ONLY_OBJECT = {OBJECT_INFO};

    private static final WrapperInfo[] WRAPPERS_NO_OBJECT = {
            CHAR_INFO,
            BOOL_INFO,
            DOUBLE_INFO,
            FLOAT_INFO,
            INT_INFO,
            LONG_INFO,
            BYTE_INFO,
            SHORT_INFO
    };

    private static final WrapperInfo[] WRAPPERS_OBJECT = {
            CHAR_INFO,
            BOOL_INFO,
            DOUBLE_INFO,
            FLOAT_INFO,
            INT_INFO,
            LONG_INFO,
            BYTE_INFO,
            SHORT_INFO,
            OBJECT_INFO
    };

    private static final WrapperInfo[] WRAPPERS_OBJECT_AND_NONE = {
            CHAR_INFO,
            BOOL_INFO,
            DOUBLE_INFO,
            FLOAT_INFO,
            INT_INFO,
            LONG_INFO,
            BYTE_INFO,
            SHORT_INFO,
            OBJECT_INFO,
            NONE_INFO
    };

    private Generate() {
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        generateBasics();

        generateAbstract();
        generateElementlockedAbstract();
        generateStorageCacheImpl();
        generateElementLockedStorageCacheImpl();

        generateImpl();
        generateDependencyNode();
        generateImplTests();

        logger.debug("Generation complete.");
    }

    private static void generateBasics() throws IOException {
        File inputPath = new File(TEMPLATE_PATH + "/interfaces");
        if (!inputPath.exists()) {
            throw new FileNotFoundException("\"" + inputPath + "\" does not exist");
        }

        File outputPath = new File(MODULE + "/src/main/java/com/maxifier/mxcache/caches");
        if (!outputPath.exists()) {
            throw new FileNotFoundException("\"" + outputPath + "\" does not exist");
        }

        generateP2P("P2PCalculatable.template", "", "Calculatable.java", inputPath, outputPath, WRAPPERS_OBJECT_AND_NONE, WRAPPERS_OBJECT);
        generateP2P("P2PCache.template", "", "Cache.java", inputPath, outputPath, WRAPPERS_OBJECT_AND_NONE, WRAPPERS_OBJECT);
    }

    private static void generateAbstract() throws IOException {
        File inputPathAbstract = new File(TEMPLATE_PATH + "/abs");
        if (!inputPathAbstract.exists()) {
            throw new FileNotFoundException("\"" + inputPathAbstract + "\" does not exist");
        }

        File outputPathStorage = new File(MODULE + "/src/main/java/com/maxifier/mxcache/storage");
        if (!outputPathStorage.exists()) {
            throw new FileNotFoundException("\"" + outputPathStorage + "\" does not exist");
        }

        File outputPathAbstract = new File(MODULE + "/src/main/java/com/maxifier/mxcache/impl/caches/abs");
        if (!outputPathAbstract.exists()) {
            throw new FileNotFoundException("\"" + outputPathAbstract + "\" does not exist");
        }

        generateP2P("P2OStorage.template", "", "Storage.java", inputPathAbstract, outputPathStorage, WRAPPERS_OBJECT_AND_NONE, ONLY_OBJECT);
        generateP2P("P2PStorage.template", "", "Storage.java", inputPathAbstract, outputPathStorage, WRAPPERS_OBJECT_AND_NONE, WRAPPERS_NO_OBJECT);

        generateP2P("P2OCache.template", "Abstract", "Cache.java", inputPathAbstract, outputPathAbstract, WRAPPERS_OBJECT_AND_NONE, ONLY_OBJECT);
        generateP2P("P2PCache.template", "Abstract", "Cache.java", inputPathAbstract, outputPathAbstract, WRAPPERS_OBJECT_AND_NONE, WRAPPERS_NO_OBJECT);
    }

    private static void generateElementlockedAbstract() throws IOException {
        File inputPathAbstract = new File(TEMPLATE_PATH + "/abs/elementlocked");
        if (!inputPathAbstract.exists()) {
            throw new FileNotFoundException("\"" + inputPathAbstract + "\" does not exist");
        }

        File outputPathStorage = new File(MODULE + "/src/main/java/com/maxifier/mxcache/storage/elementlocked");
        if (!outputPathStorage.exists()) {
            throw new FileNotFoundException("\"" + outputPathStorage + "\" does not exist");
        }

        File outputPathAbstract = new File(MODULE + "/src/main/java/com/maxifier/mxcache/impl/caches/abs/elementlocked");
        if (!outputPathAbstract.exists()) {
            throw new FileNotFoundException("\"" + outputPathAbstract + "\" does not exist");
        }

        generateP2P("P2OStorage.template", "", "ElementLockedStorage.java", inputPathAbstract, outputPathStorage, WRAPPERS_OBJECT_AND_NONE, ONLY_OBJECT);
        generateP2P("P2PStorage.template", "", "ElementLockedStorage.java", inputPathAbstract, outputPathStorage, WRAPPERS_OBJECT_AND_NONE, WRAPPERS_NO_OBJECT);

        generateP2P("P2OCache.template", "Abstract", "Cache.java", inputPathAbstract, outputPathAbstract, WRAPPERS_OBJECT_AND_NONE, ONLY_OBJECT);
        generateP2P("P2PCache.template", "Abstract", "Cache.java", inputPathAbstract, outputPathAbstract, WRAPPERS_OBJECT_AND_NONE, WRAPPERS_NO_OBJECT);
    }

    private static void generateStorageCacheImpl() throws IOException {
        File inputPathAbstract = new File(TEMPLATE_PATH + "/storage");
        if (!inputPathAbstract.exists()) {
            throw new FileNotFoundException("\"" + inputPathAbstract + "\" does not exist");
        }

        File outputPathAbstract = new File(MODULE + "/src/main/java/com/maxifier/mxcache/impl/caches/storage");
        if (!outputPathAbstract.exists()) {
            throw new FileNotFoundException("\"" + outputPathAbstract + "\" does not exist");
        }

        generateP2P("StorageP2PCache.template", "Storage", "CacheImpl.java", inputPathAbstract, outputPathAbstract, WRAPPERS_OBJECT_AND_NONE, WRAPPERS_NO_OBJECT);
        generateP2P("StorageP2OCache.template", "Storage", "CacheImpl.java", inputPathAbstract, outputPathAbstract, WRAPPERS_OBJECT_AND_NONE, ONLY_OBJECT);

        File outputPathTest = new File(MODULE + "/src/test/java/com/maxifier/mxcache/impl/caches/storage");
        if (!outputPathTest.exists()) {
            throw new FileNotFoundException("\"" + outputPathTest + "\" does not exist");
        }
        generateP2P("P2PCacheTest.template", "", "CacheTest.java", inputPathAbstract, outputPathTest, WRAPPERS_OBJECT_AND_NONE, WRAPPERS_NO_OBJECT);
        generateP2P("P2OCacheTest.template", "", "CacheTest.java", inputPathAbstract, outputPathTest, WRAPPERS_OBJECT_AND_NONE, ONLY_OBJECT);


        File inputPathGeneric = new File(TEMPLATE_PATH + "/storage/generic");
        if (!inputPathGeneric.exists()) {
            throw new FileNotFoundException("\"" + inputPathGeneric + "\" does not exist");
        }

        File outputPathGeneric = new File(MODULE + "/src/test/java/com/maxifier/mxcache/impl/caches/storage/generic");
        if (!outputPathGeneric.exists()) {
            throw new FileNotFoundException("\"" + outputPathGeneric + "\" does not exist");
        }
        generateP2P("P2PBoxKeyCacheTest.template", "", "CacheBoxKeyTest.java", inputPathGeneric, outputPathGeneric, WRAPPERS_NO_OBJECT, WRAPPERS_NO_OBJECT);
        generateP2P("P2PBoxValueCacheTest.template", "", "CacheBoxValueTest.java", inputPathGeneric, outputPathGeneric, WRAPPERS_OBJECT, WRAPPERS_NO_OBJECT);
        generateP2P("P2PBoxKeyValueCacheTest.template", "", "CacheBoxKeyValueTest.java", inputPathGeneric, outputPathGeneric, WRAPPERS_NO_OBJECT, WRAPPERS_NO_OBJECT);
        generate("P2OBoxKeyCacheTest.template", "", "ObjectCacheBoxKeyTest.java", inputPathGeneric, outputPathGeneric, WRAPPERS_NO_OBJECT);
        generate("PBoxValueCacheTest.template", "", "CacheBoxValueTest.java", inputPathGeneric, outputPathGeneric, WRAPPERS_NO_OBJECT);
        generate("PUnboxValueCacheTest.template", "", "CacheUnboxValueTest.java", inputPathGeneric, outputPathGeneric, WRAPPERS_NO_OBJECT);
    }

    private static void generateElementLockedStorageCacheImpl() throws IOException {
        File inputPathAbstract = new File(TEMPLATE_PATH + "/storage/elementlocked");
        if (!inputPathAbstract.exists()) {
            throw new FileNotFoundException("\"" + inputPathAbstract + "\" does not exist");
        }

        File outputPathAbstract = new File(MODULE + "/src/main/java/com/maxifier/mxcache/impl/caches/storage/elementlocked");
        if (!outputPathAbstract.exists()) {
            throw new FileNotFoundException("\"" + outputPathAbstract + "\" does not exist");
        }

        generateP2P("StorageP2PCache.template", "Storage", "CacheImpl.java", inputPathAbstract, outputPathAbstract, WRAPPERS_OBJECT_AND_NONE, WRAPPERS_NO_OBJECT);
        generateP2P("StorageP2OCache.template", "Storage", "CacheImpl.java", inputPathAbstract, outputPathAbstract, WRAPPERS_OBJECT_AND_NONE, ONLY_OBJECT);
    }

    private static void generateImpl() throws IOException {
        File inputPathImpl = new File(TEMPLATE_PATH + "/impl");
        if (!inputPathImpl.exists()) {
            throw new FileNotFoundException("\"" + inputPathImpl + "\" does not exist");
        }
        File outputPathImpl = new File(MODULE + "/src/main/java/com/maxifier/mxcache/impl/caches/def");
        if (!outputPathImpl.exists()) {
            throw new FileNotFoundException("\"" + outputPathImpl + "\" does not exist");
        }

        for (WrapperInfo e : WRAPPERS_OBJECT) {
            for (WrapperInfo f : WRAPPERS_OBJECT) {
                File r = new File(outputPathImpl, e.getShortName() + f.getShortName() + "CacheImpl.java");
                if (r.delete()) {
                    System.out.println("Deleted " + r);
                }
            }
        }

        generate("O2PWeakTroveStorage.template", "Object", "WeakTroveStorage.java", inputPathImpl, outputPathImpl, WRAPPERS_NO_OBJECT);
        generate("O2PTupleWeakTroveStorage.template", "Tuple", "WeakTroveStorage.java", inputPathImpl, outputPathImpl, WRAPPERS_NO_OBJECT);

        //generate("O2PTroveStorage.template", "Object", "TroveStorage.java", inputPathImpl, outputPathImpl, WRAPPERS_FULL);
        generate("P2OTroveStorage.template", "", "ObjectTroveStorage.java", inputPathImpl, outputPathImpl, WRAPPERS_NO_BOOLEAN);
        generate("B2PTroveStorage.template", "Boolean", "TroveStorage.java", inputPathImpl, outputPathImpl, WRAPPERS_NO_BOOLEAN);
        generateP2P("P2PTroveStorage.template", "", "TroveStorage.java", inputPathImpl, outputPathImpl, WRAPPERS_NO_BOOLEAN_OBJECT, WRAPPERS_NO_OBJECT);

        // BooleanCacheImpl is manually written
        generate("PStorageImpl.template", "", "StorageImpl.java", inputPathImpl, outputPathImpl, WRAPPERS_NO_OBJECT);
        generate("PInlineCacheImpl.template", "", "InlineCacheImpl.java", inputPathImpl, outputPathImpl, WRAPPERS_NO_OBJECT);
        generate("PInlineDependencyCache.template", "", "InlineDependencyCache.java", inputPathImpl, outputPathImpl, WRAPPERS_OBJECT);
    }

    private static void generateDependencyNode() throws IOException {
        File inputPath = new File(TEMPLATE_PATH + "/resource/nodes");
        if (!inputPath.exists()) {
            throw new FileNotFoundException("\"" + inputPath + "\" does not exist");
        }

        File outputPath = new File(MODULE + "/src/main/java/com/maxifier/mxcache/impl/resource/nodes");
        if (!outputPath.exists()) {
            throw new FileNotFoundException("\"" + outputPath + "\" does not exist");
        }

        generateP("ViewableMultipleP2ODependencyNode.template", "ViewableMultiple", "DependencyNode.java", inputPath, outputPath, ONLY_OBJECT);
        generateP("ViewableMultipleP2PDependencyNode.template", "ViewableMultiple", "DependencyNode.java", inputPath, outputPath, WRAPPERS_NO_OBJECT);

        generateP("ViewableSingletonP2ODependencyNode.template", "ViewableSingleton", "DependencyNode.java", inputPath, outputPath, ONLY_OBJECT);
        generateP("ViewableSingletonP2PDependencyNode.template", "ViewableSingleton", "DependencyNode.java", inputPath, outputPath, WRAPPERS_NO_OBJECT);
    }

    private static void generateImplTests() throws IOException {
        File inputPathImpl = new File(TEMPLATE_PATH + "/impl");
        if (!inputPathImpl.exists()) {
            throw new FileNotFoundException("\"" + inputPathImpl + "\" does not exist");
        }
        File outputPathImpl = new File(MODULE + "/src/test/java/com/maxifier/mxcache/impl/caches/def");
        if (!outputPathImpl.exists()) {
            throw new FileNotFoundException("\"" + outputPathImpl + "\" does not exist");
        }

        generate("P2OCacheTest.template", "", "ObjectCacheTest.java", inputPathImpl, outputPathImpl, WRAPPERS_OBJECT);
        generateP2P("P2PCacheTest.template", "", "CacheTest.java", inputPathImpl, outputPathImpl, WRAPPERS_OBJECT, WRAPPERS_NO_OBJECT);
        generate("PCacheTest.template", "", "CacheTest.java", inputPathImpl, outputPathImpl, WRAPPERS_NO_OBJECT);
    }

    private static void generate(String templateName, String pathPrefix, String pathSuffix, File inputPath, File outputPath, WrapperInfo[] wrappers) throws IOException {
        String template = readFile(templateName, inputPath);
        for (WrapperInfo e : wrappers) {
            String out = e.replaceE(replaceName(e, template));

            String outFile = pathPrefix + e.getShortName() + pathSuffix;
            writeFile(outFile, out, outputPath);
        }
    }

    private static String replaceName(WrapperInfo e, String r) {
        String pg = "$1" + e.getShortName() + "$2";
        if (e.isObject()) {
            pg += "<E>";
        }
        return r.replaceAll("([\\w\\d_]*)#EG#([\\w\\d_]*)", pg);
    }

    private static String replaceName(WrapperInfo e, WrapperInfo f, String r) {
        String pg = "$1" + e.getShortName() + "$2" + f.getShortName() + "$3";
        String tg = "$1" + e.getTroveType() + "$2" + f.getTroveType() + "$3";
        String tg1 = "$1" + e.getTroveType() + "$2";
        boolean eo = e.isObject();
        boolean fo = f.isObject();
        if (eo && !fo) {
            pg += "<E>";
            tg += "<E>";
            tg1 += "<E>";
        } else if (!eo && fo) {
            pg += "<F>";
            tg += "<F>";
        } else if (eo) {
            pg += "<E, F>";
            tg += "<E, F>";
            tg1 += "<E>";
        }
        String res = r;
        if (e.isObject() && f.isObject()) {
            res = res.replace("T#E_TROVE##F_TROVE#HashMap", "THashMap<E, F>");
        }

        res = res.replaceAll("([\\w\\d_]*)#E_TROVE#([\\w\\d_]*)#F_TROVE#([\\w\\d_]*)", tg);
        res = res.replaceAll("([\\w\\d_]*)#E_TROVE#([\\w\\d_]*)", tg1);
        return res.replaceAll("([\\w\\d_]*)#EG#([\\w\\d_]*)#FG#([\\w\\d_]*)", pg);
    }

    private static void generateP2P(String templateName, String pathPrefix, String pathSuffix, File inputPath, File outputPath, WrapperInfo[] wrappers1, WrapperInfo[] wrappers2) throws IOException {
        String template = readFile(templateName, inputPath);
        for (WrapperInfo e : wrappers1) {
            for (WrapperInfo f : wrappers2) {
                String out = replaceName(e, f, template);

                out = e.replaceE(out);
                out = f.replaceF(out);

                /*
                out = "////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////\n" +
                        "//\n" +
                        "//                       THIS IS GENERATED CLASS, DON'T EDIT THIS FILE MANUALLY!\n" +
                        "//\n" +
                        "//      generated from " + inputPath.getPath() + "\n" +
                        "//\n" +
                        "////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////\n" + out;
                */

                out = out.replace("#SOURCE#", templateName);

                String outFile = pathPrefix + e.getShortName() + f.getShortName() + pathSuffix;
                writeFile(outFile, out, outputPath);
            }
        }
    }

    private static void generateP(String templateName, String pathPrefix, String pathSuffix, File inputPath, File outputPath, WrapperInfo[] wrappers) throws IOException {
        String template = readFile(templateName, inputPath);
        for (WrapperInfo f : wrappers) {
            String out = f.replaceF(template);

            String outFile = pathPrefix + f.getShortName() + pathSuffix;
            writeFile(outFile, out, outputPath);
        }
    }

    private static void writeFile(String file, String out, File outputPath) throws IOException {
        FileUtils.writeStringToFile(new File(outputPath.getPath(), file), out);
    }

    private static String readFile(String name, File inputPath) throws IOException {
        return FileUtils.readFileToString(new File(inputPath, name));
    }


}
