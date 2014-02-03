package com.maxifier.mxcache.activity;

import javax.annotation.Nonnull;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 12:09:42
 */
public interface Activity extends Serializable {
    /**
     * Имя активности неизменно.
     *
     * @return имя ресурса.
     */
    @Nonnull
    String getName();

    /**
     * Запускает активность в заданном scope.
     * Если активность уже была запущенна в таком же или более широком scope, не делает ничего.
     * @param scope область применения активности
     */
    void start(@Nonnull ActivityScope scope);

    /**
     * Завершает активность в заданом scope.
     * Каждая активность должна быть завершена в том же scope и столько же раз, сколько была запущена.
     * Завершать thread-local активноть можно только в том же потоке, что её запустил.
     * @param scope область применения активности
     * @throws IllegalStateException если активность не была запущена в заданном scope.
     */
    void finish(@Nonnull ActivityScope scope);

    /**
     * Проверяет, запущена ли активность.
     * @return true, если активность была хотя бы раз запущена.
     */
    boolean isRunning();

    /**
     * Добавляет слушателя. Слушатель оповещается каждый раз, когда активность начинается или завершается.
     * <p>
     * В случае начала или завершения активности в thread-local scope, оповещение происходит именно в потоке,
     * вызвавшем изменение (для global scope это не обязательно).
     * Методы слушателя вызываются ПЕРЕД изменением состояния. Т.е. started может быть вызван при isRunning() == false,
     * а finished - при isRunning() == true. Исключения в слушателе (кроме Error) не влияют на работу Activity.
     * @param listener слушатель
     */
    void addListener(ActivityListener listener);

    /**
     * Удаляет слушателя.
     * @param listener слушатель
     */
    void removeListener(ActivityListener listener);
}
