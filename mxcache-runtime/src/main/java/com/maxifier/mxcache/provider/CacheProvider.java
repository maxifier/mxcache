package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.CacheId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 10.03.2010
 * Time: 9:24:10
 * </p>
 * Провайдер кэша должен уметь работать со стратегиями.
 */
public interface CacheProvider {
    /**
     * При загрузке класса с кешем, он вызывает этот метод у провайдера.
     * @param cacheOwner класс, в котором объявлен данный кэешируемый метод
     * @param cacheId идентификатор кэша
     * @param keyType тип ключа, null если кэшируемый метод не имеет параметров. Если параметров несколько, то они будут
 * оборачиваться в Tuple подходящего типа
     * @param valueType тип значения, хранимого в кэше
     * @param group группа (из аннотации @Cached метода)
     * @param tags тэги (из аннотации @Cached метода)
     * @param calculable объект, вычисляющий значение. Должен быть один из классов
     * @param methodName имя метода
     * @param methodDesc дескриптор метода
     * @param cacheName name of cache, may be null (used by some strategies)
     */
    <T> void registerCache(Class<T> cacheOwner, int cacheId, Class keyType, Class valueType, String group, String[] tags, Object calculable, String methodName, String methodDesc, @Nullable String cacheName);

    /**
     * Для статических кэшей вызывается после загрузки класса в секции инициализации, для нестатических - в
     * конструкторе.
     *
     * @param cacheOwner класс, в котором объявлен данный кэешируемый метод (может не совпадать с классом экземпляра, а
     * быть его предком)
     * @param cacheId идентификатор кэша (совпадает с тем, что был передан в registerCache)
     * @param instance экземпляр объекта с кэшем. Для статических кэшей - null.
     * @param context context
     * @return экземпляр кэша
     */
    Cache createCache(@NotNull Class cacheOwner, int cacheId, @Nullable Object instance, CacheContext context);

    CacheDescriptor getDescriptor(CacheId id);

    /**
     * @return Список всех менеджеров.
     */
    List<CacheManager> getCaches();
}
