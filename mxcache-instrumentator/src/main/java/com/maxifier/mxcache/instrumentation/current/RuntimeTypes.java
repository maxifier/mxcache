package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.*;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.context.UseCacheContext;
import com.maxifier.mxcache.resource.ResourceReader;
import com.maxifier.mxcache.resource.ResourceWriter;
import com.maxifier.mxcache.clean.ClassCacheIds;
import com.maxifier.mxcache.clean.Cleanable;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.proxy.*;

import static com.maxifier.mxcache.instrumentation.CommonRuntimeTypes.MAP_TYPE;
import static com.maxifier.mxcache.asm.Type.*;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 10.11.2010
 * Time: 10:28:19
 */
final class RuntimeTypes {
    static final Type CACHED_TYPE = getType(Cached.class);
    static final Type USE_PROXY_TYPE = getType(UseProxy.class);
    static final Type RESOURCE_READER_TYPE = getType(ResourceReader.class);
    static final Type RESOURCE_WRITER_TYPE = getType(ResourceWriter.class);
    static final Type CACHE_TYPE = getType(Cache.class);
    static final Type CLEANABLE_TYPE = getType(Cleanable.class);
    static final Type CLASS_CACHE_IDS_TYPE = getType(ClassCacheIds.class);
    static final Type MX_RESOURCE_MANAGER_TYPE = getType(MxResourceFactory.class);
    static final Type MX_RESOURCE_TYPE = getType(MxResource.class);
    static final Type RESOLVABLE_GENERATOR_TYPE = getType(ResolvableGenerator.class);
    static final Type PROXY_MANAGER_TYPE = getType(ProxyManager.class);
    static final Type PROXY_FACTORY_TYPE = getType(ProxyFactory.class);
    static final Type CACHE_FACTORY_TYPE = getType(CacheFactory.class);
    static final Type RESOLVABLE_TYPE = getType(Resolvable.class);
    static final Type CACHE_INSTRUMENTED_ANNOTATION = getType(CachedInstrumented.class);
    static final Type USE_PROXY_INSTRUMENTED_ANNOTATION = getType(UseProxyInstrumented.class);
    static final Type RESOURCE_INSTRUMENTED_ANNOTATION = getType(ResourceInstrumented.class);
    static final Type CACHE_CONTEXT_TYPE = getType(CacheContext.class);
    static final Type USE_CACHE_CONTEXT_TYPE = getType(UseCacheContext.class);

    static final Method GET_DEFAULT_CONTEXT = method("getDefaultContext", CACHE_CONTEXT_TYPE);
    static final Method GET_CONTEXT_FROM_STREAM = method("getContext", CACHE_CONTEXT_TYPE, OBJECT_TYPE);
    static final Method GET_PROXY_MANAGER_INSTANCE_METHOD = method("getInstance", PROXY_MANAGER_TYPE);

    static final Method CREATE_CACHE_METHOD_OLD = Method.getMethod("Object createCache(Class,int,Object)");
    static final Method CREATE_CACHE_METHOD = method("createCache", OBJECT_TYPE, CLASS_TYPE, INT_TYPE, OBJECT_TYPE, CACHE_CONTEXT_TYPE);

    static final Method GENERATE_RESOLVABLE = Method.getMethod("void generateResolvable(Class, int, String, String, boolean)");
    static final Method INIT_PROXY_FACTORIES_STATIC_METHOD = Method.getMethod("void $initProxyFactories$static()");
    static final Method GET_RESOURCE_METHOD = method("getResource", MX_RESOURCE_TYPE, STRING_TYPE);

    static final Method FACTORY_REGISTER_CACHE_METHOD_OLD = Method.getMethod("void registerCache(Class,int,Class,Class,String,String[],Object,String,String)");
    static final Method FACTORY_REGISTER_CACHE_METHOD = Method.getMethod("void registerCache(Class,int,Class,Class,String,String[],Object,String,String,String)");

    static final Method REGISTER_STATIC_METHOD = Method.getMethod("void registerStatic()");

    static final Method INIT_PROXY_FACTORIES_METHOD_OLD = method("$initProxyFactories$", VOID_TYPE);
    static final Method INIT_PROXY_FACTORIES_METHOD = method("$initProxyFactories$", VOID_TYPE, CACHE_CONTEXT_TYPE);

    static final Method GET_PROXY_FACTORY_METHOD_OLD = method("getProxyFactory", PROXY_FACTORY_TYPE, CLASS_TYPE, STRING_TYPE, STRING_TYPE);
    static final Method GET_PROXY_FACTORY_METHOD = method("getProxyFactory", PROXY_FACTORY_TYPE, CACHE_CONTEXT_TYPE, CLASS_TYPE, STRING_TYPE, STRING_TYPE);

    static final Method REGISTER_CACHE_OLD_METHOD = method("registerCache", VOID_TYPE);
    static final Method REGISTER_CACHE_METHOD = method("registerCache", VOID_TYPE, CACHE_CONTEXT_TYPE);

    static final Method FACTORY_REGISTER_CLASS_METHOD = method("registerClass", VOID_TYPE, CLASS_TYPE, CLEANABLE_TYPE, MAP_TYPE, MAP_TYPE);
    static final Method FACTORY_REGISTER_INSTANCE_METHOD = Method.getMethod("void registerInstance(Object,Class)");

    static final Method APPEND_STATIC_CACHES_METHOD = Method.getMethod("void appendStaticCachesTo(java.util.List)");
    static final Method APPEND_INSTANCE_CACHES_METHOD = Method.getMethod("void appendInstanceCachesTo(java.util.List,Object)");
    static final Method GET_STATIC_CACHE_METHOD = method("getStaticCache", CACHE_TYPE, INT_TYPE);
    static final Method GET_INSTANCE_CACHE_METHOD = method("getInstanceCache", CACHE_TYPE, OBJECT_TYPE, INT_TYPE);

    static final Method PROXY_METHOD = method("proxy", OBJECT_TYPE, CLASS_TYPE, RESOLVABLE_TYPE);

    private static Method method(String name, Type returnType) {
        return new Method(name, returnType, EMPTY_TYPES);
    }

    private static Method method(String name, Type returnType, Type... argTypes) {
        return new Method(name, returnType, argTypes);
    }

    private RuntimeTypes() {
    }
}
