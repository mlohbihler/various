/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.atomicjson;

/**
 * @author Matthew Lohbihler
 */
public class ParsePositionTracker {
    private int line = 1;
    private int column = 1;
    private int elementLine;
    private int elementColumn;
    private int characterCount;
    private int markedLine;
    private int markedColumn;
    private int markedCharacterCount;

    public void mark() {
        markedLine = line;
        markedColumn = column;
        markedCharacterCount = characterCount;
    }

    public void reset() {
        line = markedLine;
        column = markedColumn;
        characterCount = markedCharacterCount;
    }

    public void setElementStart() {
        elementLine = line;
        elementColumn = column;
    }

    public void update(final char c) {
        if (c == 0xA) { // Line feed
            line++;
            column = 1;
        } else
            column++;
        characterCount++;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getElementLine() {
        return elementLine;
    }

    public int getElementColumn() {
        return elementColumn;
    }

    public int getCharacterCount() {
        return characterCount;
    }
}
