/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

public class ProcessRunner {
    private static final Charset ASCII = Charset.forName("ASCII");

    private final Process process;

    public ProcessRunner(final ProcessBuilder pb) throws IOException {
        process = pb.start();
    }

    public boolean waitFor(final long timeout, final TimeUnit unit) throws InterruptedException {
        return process.waitFor(timeout, unit);
    }

    public int exitCode() {
        return process.exitValue();
    }

    public List<String> getLines() throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader(getInput()));
        final List<String> result = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null)
            result.add(line);
        return result;
    }

    public String getInput() throws IOException {
        return toString(process.getInputStream());
    }

    public String getError() throws IOException {
        return toString(process.getErrorStream());
    }

    private static String toString(final InputStream in) throws IOException {
        final StringWriter output = new StringWriter();
        IOUtils.copy(in, output, ASCII);
        return output.toString();
    }
}
