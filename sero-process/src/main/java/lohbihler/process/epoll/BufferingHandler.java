/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.process.epoll;

/**
 * Process handler that buffers the input/error streams and makes them available to the subclass.
 *
 * @author Matthew Lohbihler
 */
abstract public class BufferingHandler extends ProcessHandler {
    private final StringBuilder input = new StringBuilder();
    private final StringBuilder error = new StringBuilder();

    @Override
    public void input(final String s) {
        input.append(s);
    }

    @Override
    public void error(final String s) {
        error.append(s);
    }

    public String getInput() {
        return input.toString();
    }

    public String getError() {
        return error.toString();
    }
}
