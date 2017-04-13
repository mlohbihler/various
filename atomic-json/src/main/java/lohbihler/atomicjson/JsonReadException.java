/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.atomicjson;

/**
 * Provides information on the location within a JSON document where a read exception occurred.
 *
 * @author Matthew Lohbihler
 */
public class JsonReadException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int line;
    private final int column;

    public JsonReadException(final String message, final Throwable cause, final ParsePositionTracker tracker) {
        super(message, cause);
        this.line = tracker.getElementLine();
        this.column = tracker.getElementColumn();
    }

    public JsonReadException(final String message, final ParsePositionTracker tracker, final boolean element) {
        super(message);
        if (element) {
            this.line = tracker.getElementLine();
            this.column = tracker.getElementColumn();
        } else {
            this.line = tracker.getLine();
            this.column = tracker.getColumn();
        }
    }

    public JsonReadException(final Throwable cause, final ParsePositionTracker tracker) {
        super(cause);
        this.line = tracker.getElementLine();
        this.column = tracker.getElementColumn();
    }

    /**
     * @return the line in the JSON document where the error occurred.
     */
    public int getLine() {
        return line;
    }

    /**
     * @return the column in the JSON document where the error occurred.
     */
    public int getColumn() {
        return column;
    }

    /**
     * @return a message describing the parsing error that occurred including the line and column in the JSON document.
     */
    @Override
    public String getMessage() {
        return "line=" + line + ", column=" + column + ": " + super.getMessage();
    }
}
