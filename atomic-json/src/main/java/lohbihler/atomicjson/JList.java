/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.atomicjson;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class JList extends ArrayList<Object> {
    private static final long serialVersionUID = 1L;

    public JList() {
        // no op
    }

    public JList(final JList list) {
        super(list);
    }

    @SuppressWarnings("unchecked")
    public <T> T jget(final int index) {
        return (T) super.get(index);
    }

    public boolean getBoolean(final int index) {
        return (Boolean) jget(index);
    }

    public byte getByte(final int index) {
        return getNumber(index).byteValue();
    }

    public short getShort(final int index) {
        return getNumber(index).shortValue();
    }

    public int getInt(final int index) {
        return getNumber(index).intValue();
    }

    public long getLong(final int index) {
        return getNumber(index).longValue();
    }

    public float getFloat(final int index) {
        return getNumber(index).floatValue();
    }

    public double getDouble(final int index) {
        return getNumber(index).doubleValue();
    }

    public BigInteger getBigInteger(final int index) {
        return JUtil.toBigInteger(jget(index));
    }

    public BigDecimal getBigDecimal(final int index) {
        return JUtil.toBigDecimal(jget(index));
    }

    public Number getNumber(final int index) {
        return jget(index);
    }

    public String getString(final int index) {
        return jget(index);
    }

    public JMap getMap(final int index) {
        return jget(index);
    }

    public JList getList(final int index) {
        return jget(index);
    }

    public JList jadd(final Object value) {
        add(value);
        return this;
    }

    /**
     * @param clazz
     */
    public <E> Iterable<E> iterable(final Class<E> clazz) {
        return new Iterable<E>() {
            @Override
            public Iterator<E> iterator() {
                return new Iterator<E>() {
                    Iterator<Object> iter = JList.this.iterator();

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public E next() {
                        return (E) iter.next();
                    }
                };
            }
        };
    }

    @Override
    public boolean equals(final Object that) {
        if (that == this)
            return true;
        if (!(that instanceof List))
            return false;

        final ListIterator<Object> e1 = listIterator();
        final ListIterator<?> e2 = ((List<?>) that).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            final Object o1 = e1.next();
            final Object o2 = e2.next();
            // This is the only difference between this method and what it overrides.
            if (!(o1 == null ? o2 == null : JUtil.equals(o1, o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }
}
