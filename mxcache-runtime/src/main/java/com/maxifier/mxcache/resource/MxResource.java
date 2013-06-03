package com.maxifier.mxcache.resource;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 12.04.2010
 * Time: 9:17:37
 */
public interface MxResource extends Serializable {
    /**
     * Имя ресурса неизменно.
     * @return имя ресурса.
     */
    @NotNull
    String getName();

    /**
     * Перевести ресурс в состояние чтения.
     * Может выбросить ошибку ResourceOccupied. Не рекоммендуется её отлавливать, т.к. это может привести к
     * возникновению deadlock'ов.
     * @throws ResourceModificationException если текущий поток уже получил блокировку на запись этого ресурса, и при
     * этом сам же читает кэш.
     * Т.е. модификация ресурса НЕ ДОЛЖНА ВЫЗЫВАТЬ обращений к кэшам, зависящим от этого самого ресурса.  
     */
    void readStart() throws ResourceModificationException;

    /**
     * Освободить ресурс
     */
    void readEnd();

    /**
     * Перевести ресурс в состояние записи
     * @throws ResourceModificationException в стеке вызова находится хотя бы один кэшируемый метод. 
     */
    void writeStart() throws ResourceModificationException;

    /**
     * Освободить ресурс
     */
    void writeEnd();

    /**
     * Значени не является точным, поскольку в любой момент состояние может быть изменено в другом потоке
     * @return true, если ресурс находился в состоянии чтения
     */
    boolean isReading();

    /**
     * Значени не является точным, поскольку в любой момент состояние может быть изменено в другом потоке
     * @return true, если ресурс находился в состоянии модификации
     */
    boolean isWriting();

    /**
     * Дождаться окончания записи ресурса.
     * Этот метод не гарантирует, что ресурс будет свободен, поскольку в любой момент состояние может быть
     * изменено в другом потоке.
     * @throws ResourceModificationException если модификацию ресурса начал текущий поток (невозможно дождаться
     * окончания модификации, если сама модификация себя ждет). 
     */
    void waitForEndOfModification() throws ResourceModificationException;

    /**
     * Переводит ресурс в состояние записи и очищает все кэши, которые зависят от данного ресурса.
     */
    void clearDependentCaches();
}
