package com.maxifier.mxcache.activity;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 12:09:42
 */
public interface Activity extends Serializable {
    /**
     * »м€ активности неизменно.
     *
     * @return им€ ресурса.
     */
    @NotNull
    String getName();

    /**
     * «апускает активность в заданном scope.
     * ≈сли активность уже была запущенна в таком же или более широком scope, не делает ничего.
     * @param scope область применени€ активности
     */
    void start(@NotNull ActivityScope scope);

    /**
     * «авершает активность в заданом scope.
     *  ажда€ активность должна быть завершена в том же scope и столько же раз, сколько была запущена.
     * «авершать thread-local активноть можно только в том же потоке, что еЄ запустил.
     * @param scope область применени€ активности
     * @throws IllegalStateException если активность не была запущена в заданном scope.
     */
    void finish(@NotNull ActivityScope scope);

    /**
     * ѕровер€ет, запущена ли активность.
     * @return true, если активность была хот€ бы раз запущена.
     */
    boolean isRunning();

    /**
     * ƒобавл€ет слушател€. —лушатель оповещаетс€ каждый раз, когда активность начинаетс€ или завершаетс€.
     * <p>
     * ¬ случае начала или завершени€ активности в thread-local scope, оповещение происходит именно в потоке,
     * вызвавшем изменение (дл€ global scope это не об€зательно).
     * ћетоды слушател€ вызываютс€ ѕ≈–≈ƒ изменением состо€ни€. “.е. started может быть вызван при isRunning() == false,
     * а finished - при isRunning() == true. »сключени€ в слушателе (кроме Error) не вли€ют на работу Activity.
     * @param listener слушатель
     */
    void addListener(ActivityListener listener);

    /**
     * ”дал€ет слушател€.
     * @param listener слушатель
     */
    void removeListener(ActivityListener listener);
}
