/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.atomicjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.junit.Test;

public class JMapTest {
    @Test
    public void testGetByPath() {
        final JMap map = new JMap() //
                .put("a", true) //
                .put("b", new BigDecimal(1234)) //
                .put("c", "str") //
                .put("d", null) //
                .put("e",
                        new JList() //
                                .jadd(false) //
                                .jadd(new BigDecimal(2345)) //
                                .jadd("str2")) //
                .put("f",
                        new JMap() //
                                .put("g", "str3") //
                                .put("h",
                                        new JMap() //
                                                .put("i",
                                                        new JMap() //
                                                                .put("j", true) //
                                                                .put("k", new BigDecimal(3456)) //
                                                                .put("l", "str4") //
                                                                .put("m", null) //
                                                                .put("n", new JList()) //
                                                                .put("o", new JMap()))));

        assertEquals(true, map.getByPath("a"));
        assertEquals(new BigDecimal(1234), map.getByPath("b"));
        assertEquals("str", map.getByPath("c"));
        assertNull(map.getByPath("d"));
        assertEquals(JList.class, map.getByPath("e").getClass());

        assertEquals("str3", map.getByPath("f.g"));
        assertEquals("str3", map.getByPath("f", "g"));
        assertEquals(true, map.getByPath("f.h.i.j"));
        assertEquals(true, map.getByPath("f", "h", "i", "j"));
        assertEquals(new BigDecimal(3456), map.getByPath("f.h.i.k"));
        assertEquals(new BigDecimal(3456), map.getByPath("f", "h", "i", "k"));
        assertEquals("str4", map.getByPath("f.h.i.l"));
        assertEquals("str4", map.getByPath("f", "h", "i", "l"));
        assertNull(map.getByPath("f.h.i.m"));
        assertNull(map.getByPath("f", "h", "i", "m"));
        assertEquals(JList.class, map.getByPath("f.h.i.n").getClass());
        assertEquals(JList.class, map.getByPath("f", "h", "i", "n").getClass());
        assertEquals(JMap.class, map.getByPath("f.h.i.o").getClass());
        assertEquals(JMap.class, map.getByPath("f", "h", "i", "o").getClass());

        assertNull(map.getByPath("f.g.h"));
        assertNull(map.getByPath("f.h.i.o.q"));
        assertNull(map.getByPath("f.h.i.o.q.r"));
    }

    public void equivalence() {
        final JMap map1 = new JMap() //
                .put("a", true) //
                .put("b", new BigDecimal(1234)) //
                .put("c", "str");
        final JMap map2 = new JMap() //
                .put("a", true) //
                .put("b", 1234) //
                .put("c", "str");
        assertEquals(map1, map2);
    }
}
