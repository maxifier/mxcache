package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.*;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.config.Rule;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.proxy.UseProxy;
import com.maxifier.mxcache.proxy.ProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import com.maxifier.mxcache.transform.TransformGenerator;
import com.maxifier.mxcache.transform.TransformGeneratorFactoryImpl;
import com.maxifier.mxcache.util.CodegenHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.04.2010
 * Time: 18:05:54
 * <p>
 * Дескриптор кэша. Хранит всю необходимую информацию для менеджера кэша, чтобы создать экземпляр кэша.
 * Позволяет получать значения свойств.
 */
public class CacheDescriptor<T> {
    private static final Logger logger = LoggerFactory.getLogger(CacheDescriptor.class);

    private static final StrategyProperty<Class> USE_PROXY = StrategyProperty.create("use.proxy", Class.class, UseProxy.class, "value");

    private static final StrategyProperty<StatisticsModeEnum> STATISTICS_MODE = StrategyProperty.create("statistics.mode", StatisticsModeEnum.class, StatisticsMode.class, "value");

    private final Class<T> ownerClass;
    private final Signature signature;
    private final Signature transformedSignature;
    private final Calculable calculable;
    private final int id;
    private final Method method;
    private final String group;
    private final String[] tags;
    private final Rule rule;
    private final ProxyFactory proxyFactory;
    private final TransformGenerator keyTransform;
    private final TransformGenerator valueTransform;

    private final PropertyOverrides overrides;

    public CacheDescriptor(Class<T> ownerClass, int id, Class keyType, Class valueType, Calculable calculable, String methodName, String methodDesc, String cacheName, String group, String[] tags, ProxyFactory proxyFactory) {
        this(ownerClass, id, keyType, valueType, calculable, methodName, methodDesc, cacheName, group, tags, CacheFactory.getConfiguration().getRule(ownerClass, group, tags), proxyFactory);
    }

    public CacheDescriptor(Class<T> ownerClass, int id, Class keyType, Class valueType, Calculable calculable, String methodName, String methodDesc, String cacheName, String group, String[] tags, @NotNull Rule rule, ProxyFactory proxyFactory) {
        this(ownerClass, id, keyType, valueType, calculable, CodegenHelper.getMethod(ownerClass, methodName, methodDesc), cacheName, group, tags, rule, proxyFactory);
    }

    private CacheDescriptor(Class<T> ownerClass, int id, Class keyType, Class valueType, Calculable calculable, Method method, String cacheName, String group, String[] tags, @NotNull Rule rule, ProxyFactory proxyFactory) {
        this(ownerClass, id, signature(keyType, valueType, method), calculable, method, cacheName, group, tags, rule, proxyFactory, null);
    }

    private static Signature signature(Class keyType, Class valueType, Method method) {
        if (keyType == null) {
            return Signature.of(null, valueType);
        }
        //noinspection unchecked
        return new Signature(method.getParameterTypes(), keyType, valueType);
    }

    private CacheDescriptor(Class<T> ownerClass, int id, Signature signature, Calculable calculable, Method method, String cacheName, String group, String[] tags, @NotNull Rule rule, ProxyFactory proxyFactory, @Nullable PropertyOverrides overrides) {
        this(ownerClass, id, signature, calculable, method, cacheName, group, tags, rule, proxyFactory, TransformGeneratorFactoryImpl.getInstance().forMethod(method), TransformGenerator.NO_TRANSFORM, overrides);
    }

    private CacheDescriptor(Class<T> ownerClass, int id, Signature signature, Calculable calculable, Method method, String cacheName, String group, String[] tags, @NotNull Rule rule, ProxyFactory proxyFactory, TransformGenerator keyTransform, TransformGenerator valueTransform, PropertyOverrides overrides) {
        this(ownerClass, id, signature, calculable, method, cacheName, group, tags, rule, proxyFactory, keyTransform, valueTransform, getTransformedSignature(signature, keyTransform, valueTransform), overrides);
    }

    private static Signature getTransformedSignature(Signature signature, TransformGenerator keyTransform, TransformGenerator valueTransform) {
        return valueTransform.transformValue(keyTransform.transformKey(signature));
    }

    private CacheDescriptor(Class<T> ownerClass, int id, Signature signature, Calculable calculable, Method method, String cacheName, String group, String[] tags, @NotNull Rule rule, ProxyFactory proxyFactory, TransformGenerator keyTransform, TransformGenerator valueTransform, Signature transformedSignature, PropertyOverrides overrides) {
        this.method = method;
        this.group = group;
        this.ownerClass = ownerClass;
        this.calculable = calculable;
        this.id = id;
        this.tags = tags;
        this.rule = rule;
        this.signature = signature;
        this.proxyFactory = proxyFactory;
        this.keyTransform = keyTransform;
        this.valueTransform = valueTransform;
        this.transformedSignature = transformedSignature;
        this.overrides = overrides;
        rule.override(method, cacheName);
    }

    @PublicAPI
    public CacheDescriptor<T> overrideCalculable(Calculable calculatable) {
        Class<?> calculatableInterface = getCalculatableInterface();
        if (!calculatableInterface.isInstance(calculatable)) {
            throw new IllegalArgumentException("Calculatable for " + this + " should implement " + calculatableInterface.getName());
        }
        // cache name is already set in rule, so we pass null
        return new CacheDescriptor<T>(ownerClass, id, signature, calculatable, method, null, group, tags, rule, proxyFactory, keyTransform, valueTransform, transformedSignature, overrides);
    }

    @PublicAPI
    public CacheDescriptor<T> overrideProxyFactory(ProxyFactory factory) {
        // cache name is already set in rule, so we pass null
        return new CacheDescriptor<T>(ownerClass, id, signature, calculable, method, null, group, tags, rule, factory, keyTransform, valueTransform, transformedSignature, overrides);
    }

    public CacheDescriptor<T> overrideProxyFactory(Class<? extends ProxyFactory> factory) {
        // cache name is already set in rule, so we pass null
        return new CacheDescriptor<T>(ownerClass, id, signature, calculable, method, null, group, tags, rule, proxyFactory, keyTransform, valueTransform, transformedSignature, new PropertyOverrides(overrides).override(USE_PROXY, factory));
    }

    public Class getKeyType() {
        return signature.getContainer();
    }

    public Class getValueType() {
        return signature.getValue();
    }

    /**
     * @return "вычислятель" значений кэша. Должен реализовавыть корректный интерфейс XxxYyyCalculatable,
     * соответствующий значению KeyType и ValueType
     */
    public Calculable getCalculable() {
        return calculable;
    }

    /**
     * Этот метод интересен на самом деле только JMX.
     * @return внутренний идентификатор кэша. Методы внутри одного класса номеруются последовательно. Номерация
     * начинается с нуля.  
     */
    public int getId() {
        return id;
    }

    /**
     * @return сам кэшируемый метод
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return массив тегов. null, если тегов нет
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * @return название группы кэшей. null, если не указано
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return true, если кэш - статический
     */
    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    public boolean isResourceView() {
        return method.getAnnotation(ResourceView.class) != null;
    }

    /**
     * @return класс, содержащий описываемый метод.
     */
    public Class<T> getOwnerClass() {
        return ownerClass;
    }

    @Override
    public String toString() {
        return "mxcache:" + method;
    }

    /**
     * Читает свойство из конфигурации.
     * Корректно обрабатывает и свойства с аннотациями
     * @param property свойство
     * @param <T> тип свойства
     * @return значение свойства (возвращает значение по умолчанию, если есть)
     * @throws PropertyConvertationException если свойство в XML имеет ошибочный формат
     */
    public <T> T getProperty(StrategyProperty<T> property) throws PropertyConvertationException {
        if (overrides != null) {
            T value = overrides.get(property);
            if (value != null) {
                return value;
            }
        }
        if (property instanceof AnnotationProperty) {
            // hand-made multidispatch
            return getPropertyWithoutOverride((AnnotationProperty<?, T>) property);
        }
        return getPropertyFromRule(property);
    }

    /**
     * Читает свойство из конфигурации.
     * @param property свойство
     * @param <T> тип свойства
     * @return значение свойства (возвращает значение по умолчанию, если есть)
     * @throws PropertyConvertationException если свойство в XML имеет ошибочный формат
     */
    public <T, A extends Annotation> T getProperty(AnnotationProperty<A, T> property) throws PropertyConvertationException {
        if (overrides != null) {
            T value = overrides.get(property);
            if (value != null) {
                return value;
            }
        }
        return getPropertyWithoutOverride(property);
    }

    private <T, A extends Annotation> T getPropertyWithoutOverride(AnnotationProperty<A, T> property) {
        if (rule.isImportantProperty(property.getName())) {
            return getPropertyFromRule(property);
        }
        A annotation = method.getAnnotation(property.getAnnotationType());
        if (annotation == null) {
            return getPropertyFromRule(property);
        }
        T value = property.getFromAnnotation(annotation);
        if (value == null) {
            return getPropertyFromRule(property);
        }
        return value;
    }

    @SuppressWarnings ({ "unchecked" })
    private <T> T getPropertyFromRule(StrategyProperty<T> property) throws PropertyConvertationException {
        Object value = rule.getProperty(property.getName());
        if (value == null) {
            return property.getDefaultValue();
        }
        return (T)deconvert(property.getType(), value);
    }

    @SuppressWarnings ({ "unchecked" })
    private static Object deconvert(Class<?> type, Object value) throws PropertyConvertationException {
        try {
            if (type == String.class) {
                return value;
            } else if (type == Class.class) {
                return Class.forName((String) value);
            } else if (type == Boolean.class) {
                return Boolean.valueOf((String) value);
            } else if (type == Integer.class) {
                return Integer.valueOf((String) value);
            } else if (type == Long.class) {
                return Long.valueOf((String)value);
            } else if (type == Float.class) {
                return Float.valueOf((String) value);
            } else if (type == Double.class) {
                return Double.valueOf((String) value);
            } else if (type.isEnum()) {
                return Enum.valueOf((Class<Enum>)type, (String)value);
            } else if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                if (value instanceof List) {
                    return deconvertArray(value, componentType);
                }
                return deconvertScalarArray(value, componentType);
            }
        } catch (Exception e) {
            throw new PropertyConvertationException("Cannot convert " + value + " to " + type, e);
        }
        throw new IllegalArgumentException("Unsupported property type");
    }

    private static Object deconvertScalarArray(Object value, Class<?> componentType) throws PropertyConvertationException {
        // single-element arrays may be represented as scalar
        Object array = Array.newInstance(componentType, 1);
        Array.set(array, 0, deconvert(componentType, value));
        return array;
    }

    private static Object deconvertArray(Object value, Class<?> componentType) throws PropertyConvertationException {
        List<?> list = (List) value;
        Object array = Array.newInstance(componentType, list.size());
        int index = 0;
        for (Object o : list) {
            Array.set(array, index++, deconvert(componentType, o));
        }
        return array;
    }

    public Set<String> getResourceDependencies() {
        return rule.getResourceDependencies();
    }

    public DependencyTracking getTrackDependency() {
        return rule.getTrackDependency();
    }

    public Class<? extends CachingStrategy> getStrategyClass() {
        return rule.getStrategy();
    }

    @SuppressWarnings({ "unchecked" })
    public Class<? extends Cache> getCacheInterface() {
        return signature.getCacheInterface();
    }

    public Class<?> getCalculatableInterface() {
        return signature.getCalculableInterface();
    }

    public Signature getSignature() {
        return signature;
    }

    public StatisticsModeEnum getStatisticsMode() {
        return getProperty(STATISTICS_MODE);
    }

    private ProxyFactory getDefaultProxyFactory(CacheContext context) {
        //noinspection unchecked
        Class<ProxyFactory> proxyFactoryClass = getProperty(USE_PROXY);
        if (proxyFactoryClass == null) {
            return null;
        }
        if (!ProxyFactory.class.isAssignableFrom(proxyFactoryClass)) {
            logger.error("Proxy factory class should implement ProxyFactory: {}", proxyFactoryClass);
            return null;
        }
        return getProxyFactory(context, proxyFactoryClass);
    }

    private static ProxyFactory getProxyFactory(CacheContext context, Class<? extends ProxyFactory> proxyFactoryClass) {
        try {
            return context.getInstanceProvider().forClass(proxyFactoryClass);
        } catch (NoSuchInstanceException e) {
            logger.error("Invalid proxy factory class: " + proxyFactoryClass, e);
            return null;
        }
    }

    public ProxyFactory getProxyFactory(CacheContext context) {
        return proxyFactory == null ? getDefaultProxyFactory(context) : proxyFactory;
    }

    public TransformGenerator getKeyTransform() {
        return keyTransform;
    }

    public TransformGenerator getValueTransform() {
        return valueTransform;
    }

    public Signature getTransformedSignature() {
        return transformedSignature;
    }

    public boolean isDisabled() {
        return rule.getDisableCache();
    }

    public String getCacheName() {
        return rule.getCacheName();
    }
}
