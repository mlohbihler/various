/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.atomicjson;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Test;

public class JsonWriterTest {
    @Test
    public void map() throws IOException {
        final String before = "{\"a\":1.23,\"test\":true,\"this\":\"is\",\"simple\":null}";

        final JMap map = new JsonReader(before).read();
        final StringWriter writer = new StringWriter();
        new JsonWriter(writer).writeObject(map);
        final String after = writer.toString();

        Assert.assertEquals(before, after);
    }

    @Test
    public void list() throws IOException {
        final String before = "[\"str\",1.23,null,true]";

        final JList list = new JsonReader(before).read();
        final StringWriter writer = new StringWriter();
        new JsonWriter(writer).writeObject(list);
        final String after = writer.toString();

        Assert.assertEquals(before, after);
    }
}
