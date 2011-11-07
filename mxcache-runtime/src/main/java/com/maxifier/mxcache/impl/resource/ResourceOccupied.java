package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.resource.MxResource;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 10:47:47
 *
 * <p>
 * Эту ошибку никто не должен поймать кроме нас, поэтому не RuntimeException, а Error - слишком много любителей
 * отловить все Exception'ы.
 * <p>
 * <b>Этот эксепшн не заполняет stack trace!!!</b>
 */
public class ResourceOccupied extends Error {
    private final MxResource resource;

    public ResourceOccupied(@NotNull MxResource resource) {
        this.resource = resource;
    }

    public MxResource getResource() {
        return resource;
    }

    @Override
    public Throwable fillInStackTrace() {
        // do nothing - we only need to traverse stack, not stacktrace.
        return this;
    }

    @Override
    public String getMessage() {
        return "Resource \"" + resource + "\" is locked for write. Stack will be unrolled and topmost cached method will wait for resource to be released. This exception should not be caught";
    }
}
