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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

public class JsonReaderTest {
    @Test
    public void map() throws IOException {
        final JMap map = new JsonReader("{\"this\":\"is\", \"a\":1.23, \"simple\":null, \"test\":true}").read();
        assertEquals("is", map.get("this"));
        assertEquals(1, map.getByte("a"));
        assertEquals(1, map.getInt("a"));
        assertEquals(1, map.getLong("a"));
        assertEquals(1.23F, map.getFloat("a"), 0);
        assertEquals(1.23, map.getDouble("a"), 0);
        assertEquals(BigInteger.valueOf(1), map.getBigInteger("a"));
        assertEquals(new BigDecimal("1.23"), map.getBigDecimal("a"));
        assertEquals(new BigDecimal("1.23"), map.get("a"));
        assertNull(map.get("simple"));
        assertEquals(true, map.get("test"));
    }

    @Test
    public void number() throws IOException {
        final BigDecimal pi = new JsonReader("3.14").read();
        assertEquals(new BigDecimal("3.14"), pi);
    }

    @Test
    public void list() throws IOException {
        final JList list = new JsonReader("[\"is\", 1.23, null, true, {}, []]").read();
        assertEquals("is", list.jget(0));
        assertEquals(1, list.getByte(1));
        assertEquals(1, list.getInt(1));
        assertEquals(1, list.getLong(1));
        assertEquals(1.23F, list.getFloat(1), 0);
        assertEquals(1.23, list.getDouble(1), 0);
        assertEquals(BigInteger.valueOf(1), list.getBigInteger(1));
        assertEquals(new BigDecimal("1.23"), list.getBigDecimal(1));
        assertEquals(new BigDecimal("1.23"), list.jget(1));
        assertNull(list.jget(2));
        assertEquals(true, list.jget(3));

        final JMap map = list.jget(4);
        assertEquals(0, map.size());

        final JList list2 = list.jget(5);
        assertEquals(0, list2.size());
    }

    @Test
    public void string() throws IOException {
        final String s = new JsonReader("\"asdf\"").read();
        assertEquals("asdf", s);
    }

    @Test
    public void bool() throws IOException {
        final boolean b = new JsonReader("true").read();
        assertEquals(true, b);
    }

    @Test
    public void _null() throws IOException {
        assertNull(new JsonReader("null").read());
    }

    @Test
    public void multipleDoc() throws IOException {
        final JsonReader reader = new JsonReader("null null true false {} []");
        assertNull(reader.read());
        assertNull(reader.read());
        assertEquals(true, reader.read());
        assertEquals(false, reader.read());
        assertEquals(new JMap(), reader.read());
        assertEquals(new JList(), reader.read());
    }

    @Test
    public void danglingCommas() throws IOException {
        final JMap map = new JsonReader("{\"has\":\"an\", \"extra\":\"comma\",}").read();
        assertEquals("an", map.get("has"));
        assertEquals("comma", map.get("extra"));
    }

    @Test
    public void comments() throws IOException {
        final Reader in = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("commentTest.txt"));
        final JMap map = new JsonReader(in).read();
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    public void docTooLong() throws IOException {
        final StringReader in = new StringReader("{  \"has\"  :  \"an\"  ,   \"extra\" : \"comma\" , }");
        try {
            new JsonReader(in, 20).read();
        } catch (final JsonReadException e) {
            assertEquals("line=1, column=21: max character count exceeded", e.getMessage());
        }
    }

    @Test
    public void tracking1() throws IOException {
        final Reader in = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("trackerTest1.txt"));
        try {
            new JsonReader(in).read();
        } catch (final JsonReadException e) {
            assertEquals("line=12, column=3: EOS", e.getMessage());
        }
    }

    @Test
    public void tracking2() throws IOException {
        final Reader in = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("trackerTest2.txt"));
        try {
            new JsonReader(in).read();
        } catch (final JsonReadException e) {
            assertEquals("line=12, column=3: element is not a string: key2", e.getMessage());
        }
    }

    @Test
    public void tracking3() throws IOException {
        final Reader in = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("trackerTest3.txt"));
        try {
            new JsonReader(in).read();
        } catch (final JsonReadException e) {
            assertEquals("line=13, column=1: element is not a string: }", e.getMessage());
        }
    }

    @Test
    public void tracking4() throws IOException {
        final Reader in = new InputStreamReader(JsonReaderTest.class.getResourceAsStream("trackerTest4.txt"));
        try {
            new JsonReader(in).read();
        } catch (final JsonReadException e) {
            assertEquals("line=7, column=3: element is not a string: doh!", e.getMessage());
        }
    }

    @Test
    public void tracking5() throws IOException {
        try {
            new JsonReader("doh!").read();
        } catch (final JsonReadException e) {
            assertEquals("line=1, column=1: Value is not null, true, false, or a number: doh!", e.getMessage());
        }
    }
}
