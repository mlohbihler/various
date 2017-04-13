/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.process.epoll;

abstract public class ProcessHandler implements ProcessCallback {
    public enum DoneCause {
        FINISHED, TIMEOUT, CANCELLED, TERMINATED, EXCEPTION;
    }

    abstract public void done(DoneCause cause, int exitValue, Exception e);

    @Override
    public void finished(final int exitValue) {
        done(DoneCause.FINISHED, exitValue, null);
    }

    @Override
    public void timeout() {
        done(DoneCause.TIMEOUT, -1, null);
    }

    @Override
    public void cancelled() {
        done(DoneCause.CANCELLED, -1, null);
    }

    @Override
    public void exception(final Exception e) {
        done(DoneCause.EXCEPTION, -1, e);
    }

    @Override
    public void terminated() {
        done(DoneCause.TERMINATED, -1, null);
    }
}
