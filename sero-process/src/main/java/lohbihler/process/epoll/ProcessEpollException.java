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

public class ProcessEpollException extends IOException {
    private static final long serialVersionUID = 1L;

    private final DoneCause doneCause;
    private final int exitValue;

    public ProcessEpollException(final DoneCause doneCause, final int exitValue) {
        super();
        this.doneCause = doneCause;
        this.exitValue = exitValue;
    }

    public ProcessEpollException(final Throwable cause, final DoneCause doneCause, final int exitValue) {
        super(cause);
        this.doneCause = doneCause;
        this.exitValue = exitValue;
    }

    public DoneCause getDoneCause() {
        return doneCause;
    }

    public int getExitValue() {
        return exitValue;
    }

    @Override
    public String getMessage() {
        return "doneCause=" + doneCause + ", exitValue=" + exitValue + ", msg=" + super.getMessage();
    }
}
