/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.process;

import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;

public class FindProcess {
    public static void main(final String[] args) throws Exception {
        final long start = Clock.systemUTC().millis();
        final Process process = new ProcessBuilder("ps", "-ef").start();
        process.waitFor();
        System.out.println("Time: " + (Clock.systemUTC().millis() - start));

        final String output = toString(process.getInputStream());

        //        final StringWriter output = new StringWriter();
        //        IOUtils.copy(process.getInputStream(), output, "ASCII");

        System.out.println(output);
    }

    public static String toString(final InputStream in) throws IOException {
        final int available = in.available();
        final byte[] buf = new byte[available];
        in.read(buf);
        return new String(buf);
    }
}
