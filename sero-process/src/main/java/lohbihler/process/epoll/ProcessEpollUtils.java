/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.process.epoll;

import java.io.IOException;

import lohbihler.process.epoll.ProcessHandler.DoneCause;

public class ProcessEpollUtils {
    public static void executeAndWait(final ProcessEPoll pep, final long timeout, final String... command)
            throws IOException {
        executeAndWait(pep, timeout, new ProcessBuilder(command));
    }

    public static void executeAndWait(final ProcessEPoll pep, final long timeout, final ProcessBuilder pb)
            throws IOException {
        final long id = pep.add(pb, timeout, null);
        pep.waitFor(id);
    }

    public static String getProcessInput(final ProcessEPoll pep, final long timeout, final String... command)
            throws IOException {
        return getProcessInput(pep, timeout, new ProcessBuilder(command));
    }

    /**
     * Runs the process and blocks the thread (up to the timeout)
     *
     * @param pep
     * @param timeout
     * @param command
     * @return
     * @throws IOException
     */
    public static String getProcessInput(final ProcessEPoll pep, final long timeout, final ProcessBuilder pb)
            throws IOException {
        final ResultHandler h = new ResultHandler();

        final long id = pep.add(pb, timeout, h);
        pep.waitFor(id);

        if (h.cause != DoneCause.FINISHED)
            throw new ProcessEpollException(h.e, h.cause, h.exitValue);

        return h.input;
    }

    static class ResultHandler extends BufferingHandler {
        DoneCause cause;
        int exitValue;
        Exception e;
        String input;
        String error;

        @Override
        public void done(final DoneCause cause, final int exitValue, final Exception e) {
            this.cause = cause;
            this.exitValue = exitValue;
            this.e = e;
            input = getInput();
            error = getError();
        }
    }
}
