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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JMap extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public JMap() {
        // no op
    }

    public JMap(final JMap map) {
        super(map);
    }

    public JMap putAll(final JMap map) {
        for (final Map.Entry<String, Object> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
        return this;
    }

    /**
     * Get a generic value. This will work for objects,
     * arrays, strings, booleans, and BigDecimal. Other
     * types will need to use the type-specific getters.
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final String key) {
        return (T) super.get(key);
    }

    public JMap delete(final String... keys) {
        for (final String key : keys)
            remove(key);
        return this;
    }

    /**
     * Finds a value by path. Only works for paths through objects, not
     * arrays. Paths should be '.' delimited.
     *
     * @param path
     * @return
     */
    public <T> T getByPath(final String path) {
        return getByPath(path.split("\\."), 0);
    }

    public <T> T getByPath(final String... keys) {
        if (keys.length == 0)
            return null;

        return getByPath(keys, 0);
    }

    @SuppressWarnings("unchecked")
    private <T> T getByPath(final String[] keys, final int index) {
        final Object value = get(keys[index]);
        if (index + 1 < keys.length) {
            // More keys
            if (value == null)
                return null;

            if (value instanceof JMap)
                return ((JMap) value).getByPath(keys, index + 1);

            // Not a JMap. We could throw in protest, but for now
            // let's just return null.
            return null;
        }

        // Last key
        return (T) value;
    }

    public byte getByteByPath(final String... keys) {
        return getNumberByPath(keys).byteValue();
    }

    public short getShortByPath(final String... keys) {
        return getNumberByPath(keys).shortValue();
    }

    public int getIntByPath(final String... keys) {
        return getNumberByPath(keys).intValue();
    }

    public long getLongByPath(final String... keys) {
        return getNumberByPath(keys).longValue();
    }

    public float getFloatByPath(final String... keys) {
        return getNumberByPath(keys).floatValue();
    }

    public double getDoubleByPath(final String... keys) {
        return getNumberByPath(keys).doubleValue();
    }

    public BigInteger getBigIntegerByPath(final String... keys) {
        return JUtil.toBigInteger(getByPath(keys));
    }

    public BigDecimal getBigDecimalByPath(final String... keys) {
        return JUtil.toBigDecimal(getByPath(keys));
    }

    public Number getNumberByPath(final String... keys) {
        return getByPath(keys);
    }

    public boolean getBooleanByPath(final String... keys) {
        return getByPath(keys);
    }

    public String getStringByPath(final String... keys) {
        return getByPath(keys);
    }

    public JMap getMapByPath(final String... keys) {
        return getByPath(keys);
    }

    public JList getListByPath(final String... keys) {
        return getByPath(keys);
    }

    //
    // Get by key
    //
    public byte getByte(final String key) {
        return getNumber(key).byteValue();
    }

    public short getShort(final String key) {
        return getNumber(key).shortValue();
    }

    public int getInt(final String key) {
        return getNumber(key).intValue();
    }

    public long getLong(final String key) {
        return getNumber(key).longValue();
    }

    public float getFloat(final String key) {
        return getNumber(key).floatValue();
    }

    public double getDouble(final String key) {
        return getNumber(key).doubleValue();
    }

    public BigInteger getBigInteger(final String key) {
        return JUtil.toBigInteger(get(key));
    }

    public BigDecimal getBigDecimal(final String key) {
        return JUtil.toBigDecimal(get(key));
    }

    public Number getNumber(final String key) {
        return get(key);
    }

    public boolean getBoolean(final String key) {
        return get(key);
    }

    public String getString(final String key) {
        return get(key);
    }

    public JMap getMap(final String key) {
        return get(key);
    }

    public JList getList(final String key) {
        return get(key);
    }

    @Override
    public JMap put(final String key, final Object value) {
        super.put(key, value);
        return this;
    }

    @Override
    public boolean equals(final Object that) {
        if (that == this)
            return true;

        if (!(that instanceof Map))
            return false;
        final Map<?, ?> m = (Map<?, ?>) that;
        if (m.size() != size())
            return false;

        try {
            final Iterator<Entry<String, Object>> i = entrySet().iterator();
            while (i.hasNext()) {
                final Entry<String, Object> e = i.next();
                final String key = e.getKey();
                final Object value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    // This is the only difference between this method and what it overrides.
                    if (!JUtil.equals(value, m.get(key)))
                        return false;
                }
            }
        } catch (final ClassCastException unused) {
            return false;
        } catch (final NullPointerException unused) {
            return false;
        }

        return true;
    }
}
