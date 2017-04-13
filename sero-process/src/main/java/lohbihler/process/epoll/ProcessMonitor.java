/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.process.epoll;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.IOUtils;

/**
 * Synchronous process monitoring. The constructor blocks until the process
 * completes or times out.
 *
 * @author Matthew Lohbihler
 */
public class ProcessMonitor {
    private final String out;
    private final String err;

    public ProcessMonitor(final ProcessBuilder pb, final ExecutorService executorService, final long timeout)
            throws InterruptedException, IOException {
        this(pb.start(), executorService, timeout);
    }

    public ProcessMonitor(final Process process, final ExecutorService executorService, final long timeout)
            throws InterruptedException {
        final InputReader out = new InputReader(process.getInputStream());
        final InputReader err = new InputReader(process.getErrorStream());

        executorService.execute(out);
        executorService.execute(err);

        ProcessTimeout processTimeout = null;
        if (timeout > 0) {
            processTimeout = new ProcessTimeout(process, timeout);
            executorService.execute(processTimeout);
        }

        process.waitFor();
        out.join();
        err.join();
        process.destroy();

        // If we've made it this far, the process exited properly, so kill the
        // timeout thread if it exists.
        if (processTimeout != null)
            processTimeout.interrupt();

        this.out = out.getInput();
        this.err = err.getInput();
    }

    public String getOut() {
        return out;
    }

    public String getErr() {
        return err;
    }

    class InputReader implements Runnable {
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

    class ProcessTimeout implements Runnable {
        private final Process process;
        private final long timeout;
        private volatile boolean interrupted;

        ProcessTimeout(final Process process, final long timeout) {
            this.process = process;
            this.timeout = timeout;
        }

        public void interrupt() {
            synchronized (this) {
                interrupted = true;
                notifyAll();
            }
        }

        @Override
        public void run() {
            try {
                synchronized (this) {
                    wait(timeout);
                }

                if (!interrupted) {
                    // If the sleep time has expired, destroy the process.
                    process.destroy();
                }
            } catch (final InterruptedException e) {
                /* no op */
            }
        }
    }
}
