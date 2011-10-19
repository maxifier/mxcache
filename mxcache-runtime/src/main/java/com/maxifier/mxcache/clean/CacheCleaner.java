package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 29.03.2010
 * Time: 13:24:40
 */
public interface CacheCleaner {
    /**
     * Очищает все кэши в заданном экземпляре. Не трогает статические кэши.
     *
     * @param o экземпляр
     */
    void clearCacheByInstance(Object o);

    /**
     * Очищает все кэши в заданных экземплярах. Не трогает статические кэши.
     *
     * @param o экземпляры
     */
    void clearCacheByInstances(Object... o);

    /**
     * Очищает все кэши заданного экземпляра, имеющие заданный тег
     * @param o экземпляр
     * @param tag тег
     */
    void clearInstanceByTag(Object o, String tag);

    /**
     * Очищает все кэши заданного экземпляра, имеющие заданный группу
     *
     * @param o   экземпляр
     * @param group группа
     */
    void clearInstanceByGroup(Object o, String group);

    /**
     * Очищает все кэши в заданном классе, в том числе все кэши всех экземпляров данного класса (даже объявленные в
     * предке и в потомках), и все статические кэши, объявленные в данном классе и всех его потомках
     * (т.е. статические кэши предков не очищаются). 
     *
     * @param aClass класс
     */
    void clearCacheByClass(Class<?> aClass);

    /**
     * Очищает все кэши, принадлежащие к заданной группе
     * @param group группа
     */
    void clearCacheByGroup(String group);

    /**
     * Очищает все кэши, имеющие заданный тег
     * @param tag тег
     */
    void clearCacheByTag(String tag);

    /**
     * Очищает все кэши, аннотированные заданной аннотацией
     * @param annotationClass класс аннотации
     */
    void clearCacheByAnnotation(Class<? extends Annotation> annotationClass);

    /**
     * Очищает все кэши, заданные элементами 
     * @param elements элементы для очистки
     */
    void clearAll(Collection<? extends CleaningNode> elements);
}
