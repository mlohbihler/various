/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.atomicjson;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Writes given data as JSON to a stream. Instances should be created, used, and discarded. Reuse is generally unwise.
 *
 * @author Matthew Lohbihler
 */
public class JsonWriter {
    public static String writeToString(final Object value) {
        final StringWriter out = new StringWriter();
        final JsonWriter writer = new JsonWriter(out);
        try {
            writer.writeObject(value);
        } catch (final IOException e) {
            // This should never happen because we are writing to a StringWriter
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    /**
     * A convenience method for converting an object to JSON. By default this method will write non-optimized,
     * human-readable JSON, with line breaks and an indent of 2 spaces. This method should not be used in production
     * code (where human-readability is not required).
     *
     * @param value
     *            the object to serialize
     * @return the resulting JSON string
     * @throws JsonException
     * @throws IOException
     */
    public static String writeToPrettyString(final Object value) {
        final StringWriter out = new StringWriter();
        final JsonWriter writer = new JsonWriter(out);
        writer.setPrettyOutput(true);
        writer.setPrettyIndent(2);
        try {
            writer.writeObject(value);
        } catch (final IOException e) {
            // This should never happen because we are writing to a StringWriter
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    /**
     * The I/O writer to which the JSON content is written.
     */
    private final Writer writer;

    /**
     * Useful for preventing infinite loops in objects where there are cyclical relationships.
     */
    private boolean trackAlreadySerialized = false;

    /**
     * Whether to insert line breaks in the JSON output
     */
    private boolean prettyOutput = false;

    /**
     * Determines whether forward slashes ('/') in strings should be escaped (true) or not (false).
     */
    private boolean escapeForwardSlash = true;

    /**
     * The amount to indent pretty output. Has no effect if prettyOutput is false. Defaults to two spaces.
     */
    private String prettyIndent = "  ";

    private final List<Object> alreadySerialized = new ArrayList<>();
    private String currentIndent = "";

    public JsonWriter withTrackAlreadySerialized(final boolean trackAlreadySerialized) {
        setTrackAlreadySerialized(trackAlreadySerialized);
        return this;
    }

    public JsonWriter withEscapeForwardSlash(final boolean escapeForwardSlash) {
        setEscapeForwardSlash(escapeForwardSlash);
        return this;
    }

    public JsonWriter withPrettyOutput(final boolean prettyOutput) {
        setPrettyOutput(prettyOutput);
        return this;
    }

    public JsonWriter withPrettyIndent(final int prettyIndent) {
        setPrettyIndent(prettyIndent);
        return this;
    }

    /**
     * Creates a JSON writer with the given context around the given I/O writer.
     *
     * @param writer
     *            the I/O writer
     */
    public JsonWriter(final Writer writer) {
        this.writer = writer;
    }

    public boolean isTrackAlreadySerialized() {
        return trackAlreadySerialized;
    }

    public void setTrackAlreadySerialized(final boolean trackAlreadySerialized) {
        this.trackAlreadySerialized = trackAlreadySerialized;
    }

    public boolean isPrettyOutput() {
        return prettyOutput;
    }

    public void setPrettyOutput(final boolean prettyOutput) {
        this.prettyOutput = prettyOutput;
    }

    public int getPrettyIndent() {
        return prettyIndent.length();
    }

    public boolean isEscapeForwardSlash() {
        return escapeForwardSlash;
    }

    public void setEscapeForwardSlash(final boolean escapeForwardSlash) {
        this.escapeForwardSlash = escapeForwardSlash;
    }

    public void setPrettyIndent(final int prettyIndent) {
        if (prettyIndent <= 0)
            this.prettyIndent = "";
        else {
            this.prettyIndent = " ";
            while (this.prettyIndent.length() < prettyIndent)
                this.prettyIndent += this.prettyIndent;
            this.prettyIndent = this.prettyIndent.substring(0, prettyIndent);
        }
    }

    public void writeObjectPretty(final Object value) throws IOException {
        setPrettyOutput(true);
        setPrettyIndent(2);
        writeObject(value);
    }

    /**
     * Writes the given object as JSON to the I/O writer.
     *
     * @param value
     *            the object to write. May be null.
     * @throws JsonException
     * @throws IOException
     */
    public void writeObject(final Object value) throws IOException {
        if (value == null) {
            writer.append("null");
            return;
        }

        // Do not serialize the same object instance twice.
        if (trackAlreadySerialized) {
            for (final Object obj : alreadySerialized) {
                if (obj == value) {
                    writer.append("null");
                    return;
                }
            }
            alreadySerialized.add(value);
        }

        try {
            if (value instanceof Map<?, ?>) {
                final Map<?, ?> map = (Map<?, ?>) value;

                append('{');
                increaseIndent();

                boolean first = true;
                for (final Map.Entry<?, ?> e : map.entrySet()) {
                    if (first)
                        first = false;
                    else
                        append(',');

                    indent();
                    quote(e.getKey().toString());
                    append(':');
                    writeObject(e.getValue());
                }

                decreaseIndent();
                indent();
                append('}');
            } else if (value instanceof List<?>) {
                final List<?> list = (List<?>) value;

                append('[');
                increaseIndent();
                boolean first = true;
                for (final Object o : list) {
                    if (first)
                        first = false;
                    else
                        append(',');
                    indent();
                    writeObject(o);
                }
                decreaseIndent();
                indent();
                append(']');
            } else if (value instanceof String) {
                quote((String) value);
            } else if (value instanceof BigDecimal) {
                append(((BigDecimal) value).toPlainString());
            } else if (value instanceof Number) {
                append(((Number) value).toString());
            } else if (value instanceof Boolean) {
                append((Boolean) value ? "true" : "false");
            } else {
                throw new JsonWriteException(
                        "Don't know how to write object " + value + " of class " + value.getClass());
            }
        } catch (IOException | RuntimeException e) {
            // Let the exception through
            throw e;
        } catch (final Exception e) {
            throw new JsonWriteException("Could not write object " + value + " of class " + value.getClass(), e);
        }
    }

    /**
     * Flush the underlying I/O writer.
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Append the given character to the I/O writer. This method should not normally be used by client code.
     *
     * @param c
     * @throws IOException
     */
    public void append(final char c) throws IOException {
        writer.append(c);
    }

    /**
     * Append the given string literal to the I/O writer. This method should not normally be used by client code.
     *
     * @param c
     * @throws IOException
     */
    public void append(final String s) throws IOException {
        writer.append(s);
    }

    /**
     * Quote the given string literal and append the result to the I/O writer. This method should not normally be used
     * by client code.
     *
     * @param c
     * @throws IOException
     */
    public void quote(final String s) throws IOException {
        if (s == null) {
            writer.append("null");
            return;
        }

        final int len = s.length();
        if (len == 0) {
            writer.append("\"\"");
            return;
        }

        writer.append('"');
        for (int i = 0; i < len; i++) {
            final char c = s.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                writer.append('\\');
                writer.append(c);
                break;
            case '/':
                if (escapeForwardSlash)
                    writer.append('\\');
                writer.append(c);
                break;
            case '\b':
                writer.append("\\b");
                break;
            case '\t':
                writer.append("\\t");
                break;
            case '\n':
                writer.append("\\n");
                break;
            case '\f':
                writer.append("\\f");
                break;
            case '\r':
                writer.append("\\r");
                break;
            default:
                if (c < ' ' || c >= '\u0080' && c < '\u00a0' || c >= '\u2000' && c < '\u2100') {
                    final String t = "000" + Integer.toHexString(c);
                    writer.append("\\u" + t.substring(t.length() - 4));
                } else {
                    writer.append(c);
                }
            }
        }
        writer.append('"');
    }

    /**
     * Increase the current indenting amount. This method should not normally be used by client code.
     */
    public void increaseIndent() {
        if (prettyOutput)
            currentIndent += prettyIndent;
    }

    /**
     * Decrease the current indenting amount. This method should not normally be used by client code.
     */
    public void decreaseIndent() {
        if (prettyOutput)
            currentIndent = currentIndent.substring(0, currentIndent.length() - prettyIndent.length());
    }

    /**
     * Add the current indenting amount to the I/O writer. This method should not normally be used by client code.
     */
    public void indent() throws IOException {
        if (prettyOutput)
            writer.append("\r\n").append(currentIndent);
    }
}
