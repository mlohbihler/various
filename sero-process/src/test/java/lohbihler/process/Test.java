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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;

public class Test {
    public static void main(final String[] args) throws Exception {
        final ExecutorService executorService = Executors.newCachedThreadPool();

        final Process process = new ProcessBuilder("cmd").start();
        final InputReader input = new InputReader(process.getInputStream());
        final InputReader error = new InputReader(process.getErrorStream());

        executorService.execute(input);
        executorService.execute(error);
        final OutputStreamWriter out = new OutputStreamWriter(process.getOutputStream());

        Thread.sleep(1000);
        System.out.println("Input: " + input.getInput());
        System.out.println("Error: " + error.getInput());
        System.out.println("Alive: " + process.isAlive());
        System.out.println();

        out.append("PING 1.1.1.1 -n 1 -w 5000\r\n");
        out.flush();

        for (int i = 0; i < 7; i++) {
            Thread.sleep(1000);
            System.out.println("Input: " + input.getInput());
            System.out.println("Error: " + error.getInput());
            System.out.println("Alive: " + process.isAlive());
            System.out.println();
        }

        out.append("PING 1.1.1.1 -n 1 -w 2000\r\n");
        out.flush();

        for (int i = 0; i < 4; i++) {
            Thread.sleep(1000);
            System.out.println("Input: " + input.getInput());
            System.out.println("Error: " + error.getInput());
            System.out.println("Alive: " + process.isAlive());
            System.out.println();
        }

        process.destroy();

        executorService.shutdown();
    }

    static class InputReader implements Runnable {
        private final InputStreamReader reader;
        private final StringWriter writer = new StringWriter();
        private boolean done;

        InputReader(final InputStream is) {
            reader = new InputStreamReader(is);
        }

        public String getInput() {
            return writer.toString();
        }

        public void join() {
            synchronized (this) {
                if (!done) {
                    try {
                        wait();
                    } catch (final InterruptedException e) {
                        // no op
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                IOUtils.copy(reader, writer);
            } catch (final IOException e) {
                e.printStackTrace(new PrintWriter(writer));
            } finally {
                synchronized (this) {
                    done = true;
                    notifyAll();
                }
            }
        }
    }

}
