/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.*;
import static com.maxifier.mxcache.asm.Opcodes.*;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.reflect.Modifier;

import static com.maxifier.mxcache.instrumentation.current.RuntimeTypes.*;
import static com.maxifier.mxcache.util.CodegenHelper.*;

import com.maxifier.mxcache.instrumentation.ClassDefinition;
import com.maxifier.mxcache.instrumentation.CommonRuntimeTypes;
import com.maxifier.mxcache.instrumentation.IllegalCachedClass;
import com.maxifier.mxcache.instrumentation.InstrumentationStage;
import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.asm.commons.SerialVersionUIDAdder;
import com.maxifier.mxcache.asm.commons.TableSwitchGenerator;
import com.maxifier.mxcache.util.ClassGenerator;
import com.maxifier.mxcache.util.CodegenHelper;
import com.maxifier.mxcache.util.Generator;
import com.maxifier.mxcache.util.MxGeneratorAdapter;
import gnu.trove.THashMap;
import gnu.trove.TIntObjectIterator;
import gnu.trove.TIntObjectHashMap;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
abstract class CachedInstrumentationStage extends SerialVersionUIDAdder implements InstrumentationStage {
    private static final String INVALID_CACHE_ID_MESSAGE = "Invalid cache id";
    public static final String CLEANABLE_INNER_NAME = "Cleanable";

    private Type thisType;
    private int cacheId;

    private boolean hasReadObject;

    private String sourceFileName;

    private Type cleanableClass;

    private ClassGenerator cleanableWriter;

    private final List<ClassDefinition> additionalClasses = new ArrayList<ClassDefinition>();

    private final CachedDetector detector;

    private final InstrumentatorImpl instrumentator;

    private final Map<String, CacheIdList> byTag = new THashMap<String, CacheIdList>();

    private final Map<String, CacheIdList> byGroup = new THashMap<String, CacheIdList>();

    private final List<Generator> staticInitializers = new ArrayList<Generator>();

    private final List<Generator> instanceInitializers = new ArrayList<Generator>();

    private final TIntObjectHashMap<Generator> staticCleanMethods = new TIntObjectHashMap<Generator>();

    private final TIntObjectHashMap<Generator> instanceCleanMethods = new TIntObjectHashMap<Generator>();

    public CachedInstrumentationStage(InstrumentatorImpl instrumentator, ClassVisitor classWriter, ClassVisitor nextDetector) {
        super(Opcodes.ASM4, classWriter);
        this.instrumentator = instrumentator;
        this.detector = new CachedDetector(nextDetector);
    }

    @Override
    public ClassVisitor getDetector() {
        return detector;
    }

    @Override
    public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        if (detector.hasCachedMethods()) {
            cv = new AddStaticInitializer(cv, REGISTER_STATIC_METHOD);
            super.visit(version, access, className, signature, superName, interfaces);
            thisType = Type.getObjectType(className);

            cleanableWriter = new ClassGenerator(0, className + "$" + CLEANABLE_INNER_NAME, OBJECT_TYPE, CLEANABLE_TYPE);
            cleanableClass = cleanableWriter.getThisType();

            visitInnerClass(cleanableClass.getInternalName(), thisType.getInternalName(), CLEANABLE_INNER_NAME, ACC_PRIVATE | ACC_STATIC);
            cleanableWriter.visitOuterClass(thisType.getInternalName(), null, null);

            addMarkerAnnotation();

            cleanableWriter.defineDefaultConstructor();
        } else {
            super.visit(version, access, className, signature, superName, interfaces);
        }
    }

    private void addMarkerAnnotation() {
        instrumentator.addMarkerAnnotation(this, CACHE_INSTRUMENTED_ANNOTATION);
    }

    @Override
    public void visitSource(String source, String debug) {
        sourceFileName = source;
        super.visitSource(source, debug);
    }

    protected MxGeneratorAdapter createInitializerMethod(int acc, Method method) {
        MxGeneratorAdapter m = new MxGeneratorAdapter(visitTransparentMethod(acc, method.getName(), method.getDescriptor(), null, null), acc, method, thisType);
        m.visitCode();
        return m;
    }

    @Override
    public void visitEnd() {
        if (detector.hasCachedMethods()) {
            generateRegisterStatic();

            generateRegisterCache();

            if (!hasReadObject) {
                generateReadObject();
            }

            generateAppendStaticCaches();
            generateGetStaticCaches();

            generateAppendInstanceCaches();
            generateGetInstanceCaches();

            cleanableWriter.visitEnd();

            additionalClasses.add(new ClassDefinition(cleanableClass.getClassName(), cleanableWriter.toByteArray()));
        }
        super.visitEnd();
    }

    protected abstract void generateRegisterCache();

    private void generateRegisterStatic() {
        MxGeneratorAdapter sim = createInitializerMethod(ACC_PRIVATE | ACC_SYNTHETIC | ACC_STATIC, REGISTER_STATIC_METHOD);
        sim.push(thisType);
        sim.newInstance(cleanableClass);
        sim.dup();
        sim.invokeConstructor(cleanableClass, NO_ARG_CONSTRUCTOR);
        pushCacheIdsMap(sim, byGroup);
        pushCacheIdsMap(sim, byTag);
        sim.invokeStatic(CACHE_FACTORY_TYPE, FACTORY_REGISTER_CLASS_METHOD);

        for (Generator staticInitializer : staticInitializers) {
            staticInitializer.generate(sim);
        }

        sim.returnValue();
        sim.endMethod();
    }

    protected abstract void generateReadObject();

    private void pushCacheIdsMap(MxGeneratorAdapter sim, Map<String, CacheIdList> map) {
        if (map.isEmpty()) {
            sim.pushNull();
        } else {
            sim.newInstance(CommonRuntimeTypes.THASHMAP_TYPE);
            sim.dup();
            sim.push(map.size());
            sim.invokeConstructor(CommonRuntimeTypes.THASHMAP_TYPE, Method.getMethod("void <init>(int)"));
            for (Map.Entry<String, CacheIdList> e : map.entrySet()) {
                sim.dup();
                sim.push(e.getKey());

                sim.newInstance(CLASS_CACHE_IDS_TYPE);
                sim.dup();
                CacheIdList value = e.getValue();
                sim.push(value.getInstanceCaches());
                sim.push(value.getStaticCaches());
                sim.invokeConstructor(CLASS_CACHE_IDS_TYPE, Method.getMethod("void <init>(int[], int[])"));

                sim.invokeVirtual(CommonRuntimeTypes.THASHMAP_TYPE, Method.getMethod("Object put(Object,Object)"));
                sim.pop();
            }
        }
    }

    private void generateAppendStaticCaches() {
        MxGeneratorAdapter mv = cleanableWriter.defineMethod(ACC_PUBLIC, APPEND_STATIC_CACHES_METHOD);
        mv.start();
        for (TIntObjectIterator<Generator> it = staticCleanMethods.iterator(); it.hasNext();) {
            it.advance();            
            mv.loadArg(0);
            it.value().generate(mv);
            mv.invokeInterface(CommonRuntimeTypes.LIST_TYPE, LIST_ADD_METHOD);
            mv.pop();
        }
        mv.returnValue();
        mv.endMethod();
    }

    private void generateGetStaticCaches() {
        final MxGeneratorAdapter mv = cleanableWriter.defineMethod(ACC_PUBLIC, GET_STATIC_CACHE_METHOD);
        mv.visitCode();
        if (staticCleanMethods.isEmpty()) {
            mv.throwException(ILLEGAL_ARGUMENT_EXCEPTION_TYPE, INVALID_CACHE_ID_MESSAGE);
        } else {
            mv.loadArg(0);
            int[] keys = staticCleanMethods.keys();
            Arrays.sort(keys);
            mv.tableSwitch(keys, new TableSwitchGenerator() {
                @Override
                public void generateCase(int key, Label end) {
                    staticCleanMethods.get(key).generate(mv);
                    mv.returnValue();
                }

                @Override
                public void generateDefault() {
                    mv.throwException(ILLEGAL_ARGUMENT_EXCEPTION_TYPE, INVALID_CACHE_ID_MESSAGE);
                }
            });
        }
        mv.endMethod();
    }

    private void generateAppendInstanceCaches() {
        MxGeneratorAdapter mv = cleanableWriter.defineMethod(ACC_PUBLIC, APPEND_INSTANCE_CACHES_METHOD);
        mv.visitCode();
        mv.loadArg(1);
        mv.checkCast(thisType);
        int instance = mv.newLocal(thisType);
        mv.storeLocal(instance);
        for (TIntObjectIterator<Generator> it = instanceCleanMethods.iterator(); it.hasNext();) {
            it.advance();
            mv.loadArg(0);
            mv.loadLocal(instance);
            it.value().generate(mv);
            mv.invokeInterface(CommonRuntimeTypes.LIST_TYPE, LIST_ADD_METHOD);
            mv.pop();
        }
        mv.returnValue();
        mv.endMethod();
    }

    private void generateGetInstanceCaches() {
        final MxGeneratorAdapter mv = cleanableWriter.defineMethod(ACC_PUBLIC, GET_INSTANCE_CACHE_METHOD);
        mv.visitCode();
        if (instanceCleanMethods.isEmpty()) {
            mv.throwException(ILLEGAL_ARGUMENT_EXCEPTION_TYPE, INVALID_CACHE_ID_MESSAGE);
        } else {
            mv.loadArg(0);
            mv.checkCast(thisType);
            final int local = mv.newLocal(thisType);
            mv.storeLocal(local);
            mv.loadArg(1);
            int[] keys = instanceCleanMethods.keys();
            Arrays.sort(keys);
            mv.tableSwitch(keys, new TableSwitchGenerator() {
                @Override
                public void generateCase(int key, Label end) {
                    mv.loadLocal(local);
                    instanceCleanMethods.get(key).generate(mv);
                    mv.returnValue();
                }

                @Override
                public void generateDefault() {
                    mv.throwException(ILLEGAL_ARGUMENT_EXCEPTION_TYPE, INVALID_CACHE_ID_MESSAGE);
                }
            });
        }
        mv.endMethod();
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, String sign, String[] exceptions) {
        if (!detector.hasCachedMethods()) {
            return super.visitMethod(access, name, desc, sign, exceptions);
        }
        if (READ_OBJECT_METHOD.getName().equals(name) && READ_OBJECT_METHOD.getDescriptor().equals(desc)) {
            if (Modifier.isStatic(access)) {
                throw new IllegalCachedClass("Method readObject(ObjectInputStream) should not static", sourceFileName);
            }
            if (!Modifier.isPrivate(access)) {
                throw new IllegalCachedClass("Method readObject(ObjectInputStream) should be private", sourceFileName);
            }
            hasReadObject = true;
            return createRegistrator(access, name, desc, super.visitMethod(access, name, desc, sign, exceptions));
        }
        if (!Modifier.isStatic(access) && name.equals(CodegenHelper.CONSTRUCTOR_NAME)) {
            return createRegistrator(access, name, desc, super.visitMethod(access, name, desc, sign, exceptions));
        }
        Method method = new Method(name, desc);
        CachedMethodContext context = detector.getCachedMethodContext(method);
        if (context == null) {
            return super.visitMethod(access, name, desc, sign, exceptions);
        }
        return createMethodVisitor(access, name, desc, sign, exceptions, super.visitMethod(access, name, desc, sign, exceptions), context);
    }

    protected abstract CachedMethodVisitor createMethodVisitor(int access, String name, String desc, String sign, String[] exceptions, MethodVisitor oldVisitor, CachedMethodContext context);

    protected abstract MethodVisitor createRegistrator(int access, String name, String desc, MethodVisitor oldVisitor);

    private void appendIdList(int id, Map<String, CacheIdList> byTag, String tag, boolean isStatic) {
        CacheIdList v = byTag.get(tag);
        if (v == null) {
            v = new CacheIdList();
            byTag.put(tag, v);
        }
        (isStatic ? v.getStaticCaches() : v.getInstanceCaches()).add(id);
    }

    void appendGroupList(int id, boolean aStatic, String group) {
        appendIdList(id, byGroup, group, aStatic);
    }

    void appendTagList(int id, boolean aStatic, String tag) {
        appendIdList(id, byTag, tag, aStatic);
    }

    public int nextCache() {
        return cacheId++;
    }

    @Override
    public boolean isClassChanged() {
        return detector.hasCachedMethods();
    }

    @Override
    public List<ClassDefinition> getAdditionalClasses() {
        return additionalClasses;
    }

    // For fields that should not alter serialVersionUID
    public FieldVisitor visitTransparentField(int access, String name, String desc, String signature, Object value) {
        return cv.visitField(access, name, desc, signature, value);
    }

    // For methods that should not alter serialVersionUID
    public MethodVisitor visitTransparentMethod(int access, String name, String desc, String sign, String[] exceptions) {
        return cv.visitMethod(access, name, desc, sign, exceptions);
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public Type getThisType() {
        return thisType;
    }

    void addStaticInitializer(Generator g) {
        staticInitializers.add(g);
    }

    void addInstanceInitializer(Generator g) {
        instanceInitializers.add(g);
    }

    void applyInstanceInitializers(MxGeneratorAdapter method) {
        for (Generator instanceInitializer : instanceInitializers) {
            instanceInitializer.generate(method);
        }
    }

    public void addCacheGetter(int id, boolean isStatic, Generator cacheGetter) {
        TIntObjectHashMap<Generator> cleanMethodMap = isStatic ? staticCleanMethods : instanceCleanMethods;
        cleanMethodMap.put(id, cacheGetter);
    }
}
