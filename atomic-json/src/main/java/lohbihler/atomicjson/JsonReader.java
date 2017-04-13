/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.atomicjson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;

public class JsonReader {
    private final Reader reader;
    private final ParsePositionTracker tracker;
    private final int maxCharacterCount;

    /**
     * Convert a string of JSON data into a type graph.
     *
     * @param data
     *            the JSON data
     */
    public JsonReader(final String data) {
        this(new StringReader(data), -1);
    }

    /**
     * Convert the data in an I/O reader into a type graph.
     *
     * @param reader
     */
    public JsonReader(final Reader reader) {
        this(reader, -1);
    }

    /**
     * Convert the data in an I/O reader into a type graph.
     *
     * @param reader
     */
    public JsonReader(final Reader reader, final int maxCharacterCount) {
        Reader in = reader;
        if (!in.markSupported())
            in = new BufferedReader(reader);
        this.reader = in;

        tracker = new ParsePositionTracker();
        this.maxCharacterCount = maxCharacterCount;
    }

    /**
     * Read the next value from the input source.
     *
     * @return the value that was read.
     * @throws JsonReadException
     */
    @SuppressWarnings("unchecked")
    public <T> T read() throws JsonReadException, IOException {
        return (T) readImpl();
    }

    private Object readImpl() throws JsonReadException, IOException {
        if (testNextChar('{', true))
            return readObject();
        if (testNextChar('[', true))
            return readArray();

        final String element = nextElement();
        if (element.startsWith("\""))
            return readString(element);
        if ("null".equals(element))
            return null;
        if ("true".equals(element))
            return true;
        if ("false".equals(element))
            return false;
        try {
            return new BigDecimal(element);
        } catch (final NumberFormatException e) {
            throw new JsonReadException("Value is not null, true, false, or a number: " + element, tracker, true);
        }
    }

    private String nextChars(final int length) throws JsonReadException, IOException {
        int remaining = length;
        final StringBuilder sb = new StringBuilder();
        while (remaining-- > 0)
            sb.append(nextChar(true));
        return sb.toString();
    }

    char nextChar(final boolean throwOnEos) throws JsonReadException, IOException {
        final char c = readChar();
        if (c == 0xFFFF && throwOnEos)
            throw new JsonReadException("EOS", tracker, false);
        return c;
    }

    boolean testNextChar(final char c, final boolean throwOnEos) throws JsonReadException, IOException {
        skipWhitespace(throwOnEos);
        mark(1);
        final char n = nextChar(throwOnEos);
        final boolean result = n == c;
        reset();
        return result;
    }

    /**
     * Determines if the input source is at the end of the stream or not. Can be used in a loop where there may be
     * multiple JSON documents in a single input source.
     *
     * @return true if the end of stream has been reached.
     * @throws JsonReadException
     */
    public boolean isEos() throws JsonReadException, IOException {
        try {
            return testNextChar((char) 0xFFFF, false);
        } catch (final IOException e) {
            if ("Stream closed".equals(e.getMessage()))
                return true;
            throw e;
        }
    }

    private void skipWhitespace(final boolean throwOnEos) throws JsonReadException, IOException {
        while (true) {
            mark(2);
            char c = nextChar(throwOnEos);

            if (c == 0xFFFF && !throwOnEos) {
                //tracker.update(c);
                return;
            } else if (Character.isWhitespace(c)) {
                //tracker.update(c);
            } else if (c == '/') {
                // Check if this is a comment.
                c = nextChar(true);
                if (c == '*') {
                    // Found a block comment. Look for the terminator.
                    while (true) {
                        c = nextChar(true);
                        if (c == '*') {
                            mark(1);
                            c = nextChar(true);
                            if (c == '/')
                                // Found the terminator
                                break;
                            else if (c == 0xFFFF)
                                throw new JsonReadException("Comment terminator not found", tracker, false);
                            else
                                reset();
                        }
                    }
                } else if (c == '/') {
                    // Found a line comment. Continue until the end of the line
                    while (true) {
                        c = nextChar(false);
                        if (c == 0xA || c == 0xFFFF)
                            // End of the line
                            break;
                    }
                } else {
                    reset();
                    break;
                }
            } else {
                reset();
                break;
            }

            checkCharacterCount();
        }
    }

    String nextElement() throws JsonReadException, IOException {
        final StringBuilder sb = new StringBuilder();

        discardOptionalComma();
        skipWhitespace(false);

        tracker.setElementStart();

        char c = nextChar(true);
        sb.append(c);

        if (c == '"') {
            // Read until the next quote
            boolean done = false;
            while (!done) {
                c = nextChar(true);
                switch (c) {
                case '\\':
                    c = nextChar(true);
                    switch (c) {
                    case 'b':
                        sb.append('\b');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 'u':
                        sb.append((char) Integer.parseInt(nextChars(4), 16));
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '"':
                        sb.append('"');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    default:
                        throw new JsonReadException("Unrecognized escape character: " + c, tracker, false);
                    }
                    break;
                default:
                    sb.append(c);
                    if (c == '"')
                        done = true;
                }
                checkCharacterCount();
            }
            skipWhitespace(false);
        }

        boolean done = false;
        while (!done) {
            mark(1);
            c = readChar();
            if (c == 0xFFFF)
                break;

            switch (c) {
            case ',':
            case ']':
            case ':':
            case '}':
            case '"':
            case '/':
                reset();
                done = true;
                break;
            default:
                if (Character.isWhitespace(c))
                    done = true;
                else
                    sb.append(c);
            }

            checkCharacterCount();
        }

        return sb.toString();
    }

    void discardOptionalComma() throws JsonReadException, IOException {
        skipWhitespace(true);
        mark(1);
        final char c = nextChar(true);
        if (c != ',')
            reset();
        else
            checkCharacterCount();
    }

    private void mark(final int readAheadLimit) throws IOException {
        reader.mark(readAheadLimit);
        tracker.mark();
    }

    private void reset() throws IOException {
        reader.reset();
        tracker.reset();
    }

    private char readChar() throws IOException {
        final char c = (char) reader.read();
        tracker.update(c);
        return c;
    }

    String readString(final String element) throws JsonReadException {
        if (element.charAt(0) != '"' && element.charAt(element.length() - 1) != '"')
            throw new JsonReadException("element is not a string: " + element, tracker, true);
        return element.substring(1, element.length() - 1);
    }

    void validateNextChar(final char c) throws JsonReadException, IOException {
        skipWhitespace(true);
        final char n = nextChar(true);
        if (n != c)
            throw new JsonReadException("incorrect next character: expected '" + c + "', found '" + n + "'", tracker,
                    false);
        checkCharacterCount();
    }

    void checkCharacterCount() throws JsonReadException {
        if (maxCharacterCount != -1 && tracker.getCharacterCount() >= maxCharacterCount)
            throw new JsonReadException("max character count exceeded", tracker, false);
    }

    //
    // Native readers
    private JMap readObject() throws JsonReadException, IOException {
        final JMap object = new JMap();

        validateNextChar('{');
        while (!testNextChar('}', true)) {
            final String name = readString(nextElement());
            validateNextChar(':');
            object.put(name, readImpl());
            discardOptionalComma();
        }
        nextChar(true);

        return object;
    }

    private JList readArray() throws JsonReadException, IOException {
        final JList array = new JList();

        validateNextChar('[');
        while (!testNextChar(']', true)) {
            array.add(readImpl());
            discardOptionalComma();
        }
        nextChar(true);

        return array;
    }
}
