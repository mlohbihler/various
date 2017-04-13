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
import java.util.Iterator;
import java.util.Map;

public class JUtil {
    public static BigInteger toBigInteger(final Number n) {
        if (n == null || n instanceof BigInteger)
            return (BigInteger) n;
        if (n instanceof BigDecimal)
            return ((BigDecimal) n).toBigInteger();
        return BigInteger.valueOf(n.longValue());
    }

    public static BigDecimal toBigDecimal(final Number n) {
        if (n == null || n instanceof BigDecimal)
            return (BigDecimal) n;
        if (n instanceof BigInteger)
            return new BigDecimal((BigInteger) n);
        return BigDecimal.valueOf(n.doubleValue());
    }

    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(final T o) {
        T copy;
        if (o instanceof JMap) {
            final JMap map = (JMap) o;
            final JMap mapCopy = new JMap();
            for (final Map.Entry<String, Object> e : map.entrySet()) {
                mapCopy.put(e.getKey(), deepCopy(e.getValue()));
            }
            copy = (T) mapCopy;
        } else if (o instanceof JList) {
            final JList list = (JList) o;
            final JList listCopy = new JList();
            for (final Object e : list) {
                listCopy.add(deepCopy(e));
            }
            copy = (T) listCopy;
        } else {
            copy = o;
        }
        return copy;
    }

    public static boolean equals(final Object o1, final Object o2) {
        if (o1 == null && o2 == null)
            return true;
        if (o1 == null || o2 == null)
            return false;

        Object oo1 = o1;
        Object oo2 = o2;

        if (oo1 instanceof Number)
            oo1 = toBigDecimal((Number) oo1);
        if (oo2 instanceof Number)
            oo2 = toBigDecimal((Number) oo2);

        if (oo1 instanceof BigDecimal && oo2 instanceof BigDecimal) {
            return ((BigDecimal) oo1).compareTo((BigDecimal) oo2) == 0;
        }

        return oo1.equals(oo2);
    }

    public static JMap find(final JList list, final String attr, final Object value) {
        for (final Object o : list) {
            final JMap map = (JMap) o;
            if (JUtil.equals(value, map.get(attr)))
                return map;
        }
        return null;
    }

    public static JMap remove(final JList list, final String attr, final Object value) {
        final Iterator<Object> iter = list.iterator();
        while (iter.hasNext()) {
            final JMap map = (JMap) iter.next();
            if (JUtil.equals(value, map.get(attr))) {
                iter.remove();
                return map;
            }
        }
        return null;
    }

    public static JMap copy(final JMap from, final String... keys) {
        return copy(from, new JMap(), keys);
    }

    public static JMap copy(final JMap from, final JMap to, final String... keys) {
        for (final String key : keys) {
            if (from.containsKey(key))
                to.put(key, from.get(key));
        }
        return to;
    }
}
