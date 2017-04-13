/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.atomicjson;

public class JsonWriteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JsonWriteException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JsonWriteException(final String message) {
        super(message);
    }
}
