package com.maxifier.mxcache.ds;

import com.magenta.dataserializator.MxObjectInput;
import com.maxifier.mxcache.ContextResolver;
import com.maxifier.mxcache.context.CacheContext;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 10.03.11
 * Time: 16:22
 */
@SuppressWarnings( "UnusedDeclaration" )
// used in services
public class MxInputStreamContextResolver implements ContextResolver<MxObjectInput> {
    @Override
    public Class<MxObjectInput> getContextOwnerClass() {
        return MxObjectInput.class;
    }

    @Override
    public CacheContext getContext(MxObjectInput owner) {
        return owner.getLinkContext().getStaticValue(CacheContext.class);
    }
}
