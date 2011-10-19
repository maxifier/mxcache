package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.impl.caches.abs.elementlocked.AbstractBooleanBooleanCache;
import com.maxifier.mxcache.impl.caches.storage.StorageBooleanBooleanCacheImpl;
import com.maxifier.mxcache.legacy.MxResource;
import com.maxifier.mxcache.legacy.MxResourceManager;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 22.06.11
 * Time: 11:20
 */
@Test
public class MxAbstractResourceConverterUTest {
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidTemplate() {
        new TestMxAbstractResourceConverter("?");
    }

    public void testCreateUniqueCacheFile() {
        MxAbstractResourceConverter r = new TestMxAbstractResourceConverter("${classAbbr}_TEST_${id}");
        Assert.assertTrue(r.createUniqueCacheFile(String.class).toString().matches("s_TEST_[0-9a-f]{1,6}"));
        Assert.assertTrue(r.createUniqueCacheFile(StringBuilder.class).toString().matches("sb_TEST_[0-9a-f]{1,6}"));
        Assert.assertTrue(r.createUniqueCacheFile(AbstractBooleanBooleanCache.class).toString().matches("abbc_TEST_[0-9a-f]{1,6}"));
        Assert.assertTrue(r.createUniqueCacheFile(StorageBooleanBooleanCacheImpl.class).toString().matches("sbbc_TEST_[0-9a-f]{1,6}"));
    }

    private static class TestMxAbstractResourceConverter extends MxAbstractResourceConverter {
        public TestMxAbstractResourceConverter(String template) {
            super(mockResourceManager(), template);
        }

        @Override
        public Object convert(Object t) throws ConverterException {
            throw new UnsupportedOperationException();
        }
    }

    private static MxResourceManager mockResourceManager() {
        MxResourceManager rm = mock(MxResourceManager.class);
        when(rm.<MxResource>getTempResource(any(String.class))).thenAnswer(new Answer<MxResource>() {
            @Override
            public MxResource answer(InvocationOnMock invocation) throws Throwable {
                MxResource res = mock(MxResource.class);
                when(res.toString()).thenReturn((String) invocation.getArguments()[0]);
                return res;
            }
        });
        return rm;
    }
}
