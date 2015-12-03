/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.instrumentation.*;
import com.maxifier.mxcache.util.SmartClassWriter;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class InstrumentatorImpl implements com.maxifier.mxcache.instrumentation.Instrumentator {
    static final String CACHED_DESCRIPTOR = RuntimeTypes.CACHED_TYPE.getDescriptor();
    static final String RESOURCE_READER_DESCRIPTOR = RuntimeTypes.RESOURCE_READER_TYPE.getDescriptor();
    static final String RESOURCE_WRITER_DESCRIPTOR = RuntimeTypes.RESOURCE_WRITER_TYPE.getDescriptor();
    static final String USE_PROXY_DESCRIPTOR = RuntimeTypes.USE_PROXY_TYPE.getDescriptor();
    static final String HASHING_STRATEGY_DESCRIPTOR = RuntimeTypes.HASHING_STRATEGY_TYPE.getDescriptor();
    static final String IDENTITY_HASHING_DESCRIPTOR = RuntimeTypes.IDENTITY_HASHING_TYPE.getDescriptor();
    
    private static final BytecodeMatcher CACHED_DESCRIPTOR_MATCHER = new BytecodeMatcher(CACHED_DESCRIPTOR);
    private static final BytecodeMatcher RESOURCE_READER_DESCRIPTOR_MATCHER = new BytecodeMatcher(RESOURCE_READER_DESCRIPTOR);
    private static final BytecodeMatcher RESOURCE_WRITER_DESCRIPTOR_MATCHER = new BytecodeMatcher(RESOURCE_WRITER_DESCRIPTOR);
    private static final BytecodeMatcher USE_PROXY_DESCRIPTOR_MATCHER = new BytecodeMatcher(USE_PROXY_DESCRIPTOR);

    public static final Instrumentator[] VERSIONS = new Instrumentator[] {new InstrumentatorImpl(false, "2.1.9") {
        @Override
        protected CachedInstrumentationStage createCachedStage(ClassVisitor visitor, ClassVisitor detector) {
            return new CachedInstrumentationStage219(this, visitor, detector);
        }

        @Override
        protected UseProxyInstrumentationStage createProxyStage(ClassVisitor visitor, ClassVisitor detector) {
            return new UseProxyInstrumentationStage219(this, visitor, detector);
        }

        protected ResourceInstrumentationStage createResourceStage(ClassVisitor visitor, ClassVisitor detector) {
            return new ResourceInstrumentationStage219(this, visitor, detector);
        }
    }, new InstrumentatorImpl(true, "2.2.9") {
        @Override
        protected CachedInstrumentationStage createCachedStage(ClassVisitor visitor, ClassVisitor detector) {
            return new CachedInstrumentationStage229(this, visitor, detector);
        }

        @Override
        protected UseProxyInstrumentationStage createProxyStage(ClassVisitor visitor, ClassVisitor detector) {
            return new UseProxyInstrumentationStage229(this, visitor, detector);
        }

        protected ResourceInstrumentationStage createResourceStage(ClassVisitor visitor, ClassVisitor detector) {
            return new ResourceInstrumentationStage219(this, visitor, detector);
        }
    }, new InstrumentatorImpl(true, "2.2.28") {
        @Override
        protected CachedInstrumentationStage createCachedStage(ClassVisitor visitor, ClassVisitor detector) {
            return new CachedInstrumentationStage229(this, visitor, detector);
        }

        @Override
        protected UseProxyInstrumentationStage createProxyStage(ClassVisitor visitor, ClassVisitor detector) {
            return new UseProxyInstrumentationStage229(this, visitor, detector);
        }

        protected ResourceInstrumentationStage createResourceStage(ClassVisitor visitor, ClassVisitor detector) {
            return new ResourceInstrumentationStage2228(this, visitor, detector);
        }
    }, new InstrumentatorImpl(true, "2.6.2") {
        @Override
        protected CachedInstrumentationStage createCachedStage(ClassVisitor visitor, ClassVisitor detector) {
            return new CachedInstrumentationStage262(this, visitor, detector);
        }
        @Override
        protected UseProxyInstrumentationStage createProxyStage(ClassVisitor visitor, ClassVisitor detector) {
            return new UseProxyInstrumentationStage229(this, visitor, detector);
        }
        @Override
        protected ResourceInstrumentationStage createResourceStage(ClassVisitor visitor, ClassVisitor detector) {
            return new ResourceInstrumentationStage219(this, visitor, detector);
        }
    }};

    private final String version;

    private final boolean addMarkerAnnotations;

    private InstrumentatorImpl(boolean addMarkerAnnotations, String version) {
        this.addMarkerAnnotations = addMarkerAnnotations;
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    void addMarkerAnnotation(ClassVisitor classVisitor, Type annotationType) {
        if (addMarkerAnnotations) {
            AnnotationVisitor visitor = classVisitor.visitAnnotation(annotationType.getDescriptor(), true);
            visitor.visit("compatibleVersion", MxCache.getCompatibleVersion());
            visitor.visit("version", MxCache.getVersion());
            visitor.visitEnd();
        }
    }

    private interface StageFactory {
        boolean fastDetect(byte[] bytecode);

        InstrumentationStage createStage(ClassVisitor visitor, ClassVisitor detector);
    }
    
    private final StageFactory[] stageFactories = {
            new StageFactory() {
                @Override
                public boolean fastDetect(byte[] bytecode) {
                    return RESOURCE_READER_DESCRIPTOR_MATCHER.isContainedIn(bytecode) ||
                            RESOURCE_WRITER_DESCRIPTOR_MATCHER.isContainedIn(bytecode);
                }

                @Override
                public InstrumentationStage createStage(ClassVisitor visitor, ClassVisitor detector) {
                    return createResourceStage(visitor, detector);
                }
            },
            new StageFactory() {
                @Override
                public boolean fastDetect(byte[] bytecode) {
                    return CACHED_DESCRIPTOR_MATCHER.isContainedIn(bytecode);
                }

                @Override
                public InstrumentationStage createStage(ClassVisitor visitor, ClassVisitor detector) {
                    return createCachedStage(visitor, detector);
                }
            },

            // CachedInstrumentationStage removes @Cached annotations so it's impossible to differentiate
            // @UseProxy w/ @Cached and @UseProxy w/o @Cached after it, so we place proxy instrumentation stage
            // before it
            new StageFactory() {
                @Override
                public boolean fastDetect(byte[] bytecode) {
                    return USE_PROXY_DESCRIPTOR_MATCHER.isContainedIn(bytecode);
                }

                @Override
                public InstrumentationStage createStage(ClassVisitor visitor, ClassVisitor detector) {
                    return createProxyStage(visitor, detector);
                }
            }
    };

    /**
     * Instruments the bytecode
     * @param bytecode original class bytecode
     * @return instrumented bytecode among with generated stuff classes, null if no instrumentation is required.
     * @throws com.maxifier.mxcache.instrumentation.IllegalCachedClass if there are incorrect annotated methods
     *  (e.g. native or abstract methods with @Cached)
     */
    @Override
    public ClassInstrumentationResult instrument(byte[] bytecode) {
        List<StageFactory> activeFactories = findMatchingStages(bytecode);
        if (activeFactories.isEmpty()) {
            return null;
        }
        ClassReader classReader = new ClassReader(bytecode);
        ClassWriter classWriter = new SmartClassWriter(classReader);

        List<InstrumentationStage> stages = new ArrayList<InstrumentationStage>();
        ClassVisitor visitor = classWriter;
        ClassVisitor detector = new ClassVisitor(Opcodes.ASM4) {};

        // instrumentation stages are stacked, last added is first passed to class reader
        for (StageFactory factory : activeFactories) {
            InstrumentationStage stage = factory.createStage(visitor, detector);
            stages.add(stage);
            visitor = (ClassVisitor)stage;
            detector = stage.getDetector();
        }
        classReader.accept(detector, ClassReader.SKIP_FRAMES);

        classReader.accept(visitor, ClassReader.SKIP_FRAMES);

        if (isClassChanged(stages)) {
            byte[] bytes = classWriter.toByteArray();
            List<ClassDefinition> classes = getAdditionalClasses(stages);

//            ClassLoader cl = new ClassLoader() {};
//            for (ClassDefinition c : classes) {
//                CodegenHelper.verify(c.getBytecode());
//                CodegenHelper.loadClass(cl, c.getBytecode());
//            }
//            CodegenHelper.dumpClass(bytes);
//            CodegenHelper.verify(bytes, cl);
            
            return new ClassInstrumentationResult(bytes, classes);
        }
        return null;
    }

    private List<StageFactory> findMatchingStages(byte[] bytecode) {
        List<StageFactory> activeFactories = new ArrayList<StageFactory>();
        for (StageFactory stageFactory : stageFactories) {
            if (stageFactory.fastDetect(bytecode)) {
                activeFactories.add(stageFactory);
            }
        }
        return activeFactories;
    }

    protected abstract ResourceInstrumentationStage createResourceStage(ClassVisitor visitor, ClassVisitor detector);

    protected abstract UseProxyInstrumentationStage createProxyStage(ClassVisitor visitor, ClassVisitor detector);

    protected abstract CachedInstrumentationStage createCachedStage(ClassVisitor visitor, ClassVisitor detector);

    private static boolean isClassChanged(List<InstrumentationStage> stages) {
        for (InstrumentationStage stage : stages) {
            if (stage.isClassChanged()) {
                return true;
            }
        }
        return false;
    }

    private static List<ClassDefinition> getAdditionalClasses(List<InstrumentationStage> stages) {
        List<ClassDefinition> res = new ArrayList<ClassDefinition>();
        for (InstrumentationStage stage : stages) {
            res.addAll(stage.getAdditionalClasses());
        }
        return res;
    }

    @Override
    public String toString() {
        return "Instrumentator<" + version + ">";
    }
}
