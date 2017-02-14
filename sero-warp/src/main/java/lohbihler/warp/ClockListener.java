/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.warp;

import java.time.Instant;

@FunctionalInterface
public interface ClockListener {
    /**
     * @param instant
     *            the instant at which this method is called
     */
    void clockUpdate(Instant instant);
}
