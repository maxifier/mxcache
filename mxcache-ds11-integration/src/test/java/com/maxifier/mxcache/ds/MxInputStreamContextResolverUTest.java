package com.maxifier.mxcache.ds;

import com.magenta.dataserializator.MxObjectInput;
import com.magenta.dataserializator.link.LinkContext;
import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.context.CacheContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 10.03.11
 * Time: 16:28
 */
@Test
public class MxInputStreamContextResolverUTest {
    @Test
    public void testGetContext() throws Exception {
        MxObjectInput input = mock(MxObjectInput.class);
        LinkContext linkContext = mock(LinkContext.class);
        when(input.getLinkContext()).thenReturn(linkContext);
        CacheContext context = mock(CacheContext.class);
        when(linkContext.getStaticValue(CacheContext.class)).thenReturn(context);

        Assert.assertSame(CacheFactory.getContext(input), context);
    }
}
