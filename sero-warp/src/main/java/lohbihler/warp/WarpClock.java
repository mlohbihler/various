/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.warp;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class WarpClock extends Clock {
    private LocalDateTime dateTime;
    private final List<ClockListener> listeners = new CopyOnWriteArrayList<>();
    private ZoneOffset offset = ZoneOffset.UTC;

    public WarpClock() {
        this(LocalDateTime.now());
    }

    public WarpClock(final LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime");
        this.dateTime = dateTime;
    }

    public TimeoutFuture<?> setTimeout(final Runnable command, final long timeout, final TimeUnit timeUnit) {
        return setTimeout(() -> {
            command.run();
            return null;
        }, timeout, timeUnit);
    }

    public <V> TimeoutFuture<V> setTimeout(final Callable<V> callable, final long timeout, final TimeUnit timeUnit) {
        final Instant deadline = dateTime.plusNanos(timeUnit.toNanos(timeout)).toInstant(offset);
        final TimeoutFutureImpl<V> future = new TimeoutFutureImpl<>();
        final ClockListener listener = new ClockListener() {
            @Override
            public void clockUpdate(final Instant instant) {
                if (!instant.isBefore(deadline)) {
                    if (!future.isCancelled()) {
                        try {
                            future.setResult(callable.call());
                        } catch (final Exception e) {
                            future.setException(e);
                        }
                    }
                    listeners.remove(this);
                }
            }
        };
        listeners.add(listener);
        return future;
    }

    class TimeoutFutureImpl<V> implements TimeoutFuture<V> {
        private boolean success;
        private boolean cancelled;
        private Exception ex;
        private V result;
        private volatile boolean done;

        @Override
        public V get() throws CancellationException, InterruptedException, Exception {
            if (success)
                return result;
            if (ex != null)
                throw ex;
            if (cancelled)
                throw new CancellationException();

            synchronized (this) {
                wait();
            }

            if (success)
                return result;
            if (ex != null)
                throw ex;
            throw new CancellationException();
        }

        public synchronized boolean isCancelled() {
            return cancelled;
        }

        @Override
        public synchronized boolean cancel() {
            if (!done) {
                cancelled = true;
                done();
                return true;
            }
            return false;
        }

        synchronized void setResult(final V result) {
            this.result = result;
            success = true;
            done();
        }

        synchronized void setException(final Exception ex) {
            this.ex = ex;
            done();
        }

        void done() {
            notifyAll();
            done = true;
        }
    }

    public void addListener(final ClockListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final ClockListener listener) {
        listeners.remove(listener);
    }

    public LocalDateTime plus(final TemporalAmount amountToAdd) {
        return fireUpdate(dateTime.plus(amountToAdd));
    }

    public LocalDateTime plus(final long amountToAdd, final TemporalUnit unit) {
        return fireUpdate(dateTime.plus(amountToAdd, unit));
    }

    public LocalDateTime plusYears(final long years) {
        return fireUpdate(dateTime.plusYears(years));
    }

    public LocalDateTime plusMonths(final long months) {
        return fireUpdate(dateTime.plusMonths(months));
    }

    public LocalDateTime plusWeeks(final long weeks) {
        return fireUpdate(dateTime.plusWeeks(weeks));
    }

    public LocalDateTime plusDays(final long days) {
        return fireUpdate(dateTime.plusDays(days));
    }

    public LocalDateTime plusHours(final long hours) {
        return fireUpdate(dateTime.plusHours(hours));
    }

    public LocalDateTime plusMinutes(final long minutes) {
        return fireUpdate(dateTime.plusMinutes(minutes));
    }

    public LocalDateTime plusSeconds(final long seconds) {
        return fireUpdate(dateTime.plusSeconds(seconds));
    }

    public LocalDateTime plusMillis(final long millis) {
        return fireUpdate(dateTime.plusNanos(millis * 1_000_000L));
    }

    public LocalDateTime plusNanos(final long nanos) {
        return fireUpdate(dateTime.plusNanos(nanos));
    }

    private LocalDateTime fireUpdate(final LocalDateTime newDateTime) {
        dateTime = newDateTime;
        final Instant instant = instant();
        for (final ClockListener l : listeners) {
            l.clockUpdate(instant);
        }
        return dateTime;
    }

    @Override
    public ZoneId getZone() {
        return offset;
    }

    @Override
    public Clock withZone(final ZoneId zone) {
        final ZoneId normalized = zone.normalized();
        if (!(normalized instanceof ZoneOffset))
            throw new RuntimeException("Cannot normalize " + zone + " to a ZoneOffset");
        this.offset = (ZoneOffset) normalized;
        return this;
    }

    @Override
    public Instant instant() {
        return dateTime.toInstant(offset);
    }
}
