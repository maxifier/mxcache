package com.maxifier.mxcache.util;

import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.11.2010
 * Time: 17:37:46
 */
@Test
public class THashMapBuilderUTest {
    public void testAdd() {
        THashMapBuilder<String, String> builder = new THashMapBuilder<String, String>();
        Map<String,String> map = builder.put("123", "321").toMap();
        assertEquals(map.get("123"), "321");
        assertEquals(map.size(), 1);
        builder.put("312", "123");
        assertEquals(map.size(), 1);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testModify() {
        new THashMapBuilder<String, String>().put("123", "312").toMap().put("123", "321");
    }
}
