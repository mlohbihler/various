/*
 * Copyright (c) 2017, Matthew Lohbihler
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package lohbihler.sim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import lohbihler.warp.WarpClock;
import lohbihler.warp.WarpScheduledExecutorService;

public class WarpScheduledExecutorServiceTest {
    private WarpClock clock;
    private Instant start;
    private WarpScheduledExecutorService executorService;

    @Before
    public void before() {
        clock = new WarpClock();
        start = clock.instant();
        executorService = new WarpScheduledExecutorService(clock);
    }

    @Test
    public void callables() throws InterruptedException {
        final ScheduledFuture<Boolean> success = executorService.schedule(() -> true, 100, TimeUnit.MINUTES);
        final ScheduledFuture<Boolean> timeout = executorService.schedule(() -> true, 100, TimeUnit.MINUTES);
        final ScheduledFuture<Boolean> exception = executorService.schedule(() -> {
            throw new Exception("test ex");
        }, 100, TimeUnit.MINUTES);
        final ScheduledFuture<Boolean> cancel = executorService.schedule(() -> true, 100, TimeUnit.MINUTES);

        // Create threads to get future results.
        final AtomicBoolean successResult = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                assertTrue(success.get());
                assertEquals(Duration.ofMinutes(100), Duration.between(start, clock.instant()));
                successResult.set(true);
            } catch (final Exception e) {
                fail(e.getMessage());
            }
        }).start();

        final AtomicBoolean timeoutResult = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                timeout.get(70, TimeUnit.MINUTES);
                fail("Should have timed out");
            } catch (final TimeoutException e) {
                assertEquals(Duration.ofMinutes(70), Duration.between(start, clock.instant()));
                timeoutResult.set(true);
            } catch (final Exception e) {
                fail(e.getMessage());
            }
        }).start();

        final AtomicBoolean exceptionResult = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                exception.get(101, TimeUnit.MINUTES);
                fail("Should have thrown an exception");
            } catch (final ExecutionException e) {
                assertEquals("test ex", e.getCause().getMessage());
                assertEquals(Duration.ofMinutes(100), Duration.between(start, clock.instant()));
                exceptionResult.set(true);
            } catch (final Exception e) {
                fail(e.getMessage());
            }
        }).start();

        final AtomicBoolean cancelResult = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                cancel.get(51, TimeUnit.MINUTES);
                fail("Should have been cancelled");
            } catch (final CancellationException e) {
                assertEquals(Duration.ofMinutes(50), Duration.between(start, clock.instant()));
                cancelResult.set(true);
            } catch (final Exception e) {
                fail(e.getMessage());
            }
        }).start();

        // Let the threads get started
        Thread.sleep(5);

        // Advance the clock 50 minutes and cancel. Do so in a loop for more realistic testing.
        for (int i = 0; i < 50; i++) {
            clock.plusMinutes(1);
            Thread.sleep(5);
        }
        cancel.cancel(false);
        Thread.sleep(5);

        // Advance the clock another 50 minutes.
        for (int i = 0; i < 50; i++) {
            clock.plusMinutes(1);
            Thread.sleep(5);
        }

        assertFalse(success.isCancelled());
        assertFalse(timeout.isCancelled());
        assertFalse(exception.isCancelled());
        assertTrue(cancel.isCancelled());

        assertTrue(successResult.get());
        assertTrue(timeoutResult.get());
        assertTrue(exceptionResult.get());
        assertTrue(cancelResult.get());
    }

    @Test
    public void fixedRate() throws InterruptedException {
        final List<Instant> instants1 = new ArrayList<>();
        final ScheduledFuture<?> future1 = executorService.scheduleAtFixedRate(() -> instants1.add(clock.instant()), 3,
                2, TimeUnit.MINUTES);
        final List<Instant> instants2 = new ArrayList<>();
        final ScheduledFuture<?> future2 = executorService.scheduleAtFixedRate(() -> instants2.add(clock.instant()), 4,
                2, TimeUnit.MINUTES);

        // Run the minutes individually to ensure the runtimes.
        for (int i = 0; i < 7; i++) {
            clock.plusMinutes(1);
        }

        assertEquals(3, instants1.size());
        assertEquals(Duration.ofMinutes(3), Duration.between(start, instants1.get(0)));
        assertEquals(Duration.ofMinutes(5), Duration.between(start, instants1.get(1)));
        assertEquals(Duration.ofMinutes(7), Duration.between(start, instants1.get(2)));

        assertEquals(2, instants2.size());
        assertEquals(Duration.ofMinutes(4), Duration.between(start, instants2.get(0)));
        assertEquals(Duration.ofMinutes(6), Duration.between(start, instants2.get(1)));

        // Advance the clock by multiple minutes. The instants in the list will all have the end time, not the proper
        // run times.
        clock.plusHours(1);
        assertEquals(33, instants1.size());
        assertEquals(32, instants2.size());

        // Cancel a task, and ensure that no more runs occur.
        future1.cancel(false);
        clock.plusHours(1);
        assertEquals(33, instants1.size());
        assertEquals(62, instants2.size());

        final AtomicBoolean cancelSuccess = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                future2.get();
                fail("Should have thrown CancellationException");
            } catch (final CancellationException e) {
                cancelSuccess.set(true);
            } catch (final Exception e) {
                fail(e.getMessage());
            }
        }).start();

        Thread.sleep(50);
        future2.cancel(false);
        Thread.sleep(50);

        assertTrue(cancelSuccess.get());
    }

    @Test
    public void fixedDelay() throws InterruptedException {
        final List<Instant> instants1 = new ArrayList<>();
        final ScheduledFuture<?> future1 = executorService.scheduleWithFixedDelay(() -> instants1.add(clock.instant()),
                3, 2, TimeUnit.MINUTES);
        final List<Instant> instants2 = new ArrayList<>();
        final ScheduledFuture<?> future2 = executorService.scheduleWithFixedDelay(() -> instants2.add(clock.instant()),
                4, 2, TimeUnit.MINUTES);

        // Run the minutes individually to ensure the runtimes.
        for (int i = 0; i < 7; i++) {
            clock.plusMinutes(1);
        }

        assertEquals(3, instants1.size());
        assertEquals(Duration.ofMinutes(3), Duration.between(start, instants1.get(0)));
        assertEquals(Duration.ofMinutes(5), Duration.between(start, instants1.get(1)));
        assertEquals(Duration.ofMinutes(7), Duration.between(start, instants1.get(2)));

        assertEquals(2, instants2.size());
        assertEquals(Duration.ofMinutes(4), Duration.between(start, instants2.get(0)));
        assertEquals(Duration.ofMinutes(6), Duration.between(start, instants2.get(1)));

        // Advance the clock by multiple minutes. Because these are delayed, each will have run only once.
        clock.plusHours(1);
        assertEquals(4, instants1.size());
        assertEquals(3, instants2.size());

        // Cancel a task, and ensure that no more runs occur.
        future1.cancel(false);
        clock.plusHours(1);
        assertEquals(4, instants1.size());
        assertEquals(4, instants2.size());

        final AtomicBoolean cancelSuccess = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                future2.get();
                fail("Should have thrown CancellationException");
            } catch (final CancellationException e) {
                cancelSuccess.set(true);
            } catch (final Exception e) {
                fail(e.getMessage());
            }
        }).start();

        Thread.sleep(50);
        future2.cancel(false);
        Thread.sleep(50);

        assertTrue(cancelSuccess.get());
    }

    @Test
    public void scheduled() {
        final List<Instant> instants = new ArrayList<>();

        final ScheduledFuture<?> future = executorService.schedule(() -> instants.add(clock.instant()), 10,
                TimeUnit.MINUTES);
        executorService.schedule((Runnable) () -> instants.add(clock.instant()), 11, TimeUnit.MINUTES);
        executorService.schedule((Runnable) () -> instants.add(clock.instant()), 12, TimeUnit.MINUTES);
        executorService.schedule((Runnable) () -> instants.add(clock.instant()), 15, TimeUnit.MINUTES);
        executorService.schedule((Runnable) () -> instants.add(clock.instant()), 20, TimeUnit.MINUTES);
        executorService.schedule((Runnable) () -> instants.add(clock.instant()), 25, TimeUnit.MINUTES);
        executorService.schedule((Runnable) () -> instants.add(clock.instant()), 26, TimeUnit.MINUTES);
        executorService.schedule((Runnable) () -> instants.add(clock.instant()), 27, TimeUnit.MINUTES);
        executorService.schedule((Runnable) () -> instants.add(clock.instant()), 28, TimeUnit.MINUTES);
        executorService.schedule((Runnable) () -> instants.add(clock.instant()), 30, TimeUnit.MINUTES);

        clock.plusMinutes(9);
        assertEquals(0, instants.size());

        clock.plusMinutes(1);
        assertEquals(1, instants.size());

        clock.plusMinutes(5);
        assertEquals(4, instants.size());

        clock.plusMinutes(10);
        assertEquals(6, instants.size());

        clock.plusMinutes(10);
        assertEquals(10, instants.size());

        assertEquals(Duration.ofMinutes(10), Duration.between(start, instants.get(0)));
        assertEquals(Duration.ofMinutes(15), Duration.between(start, instants.get(1)));
        assertEquals(Duration.ofMinutes(15), Duration.between(start, instants.get(2)));
        assertEquals(Duration.ofMinutes(15), Duration.between(start, instants.get(3)));
        assertEquals(Duration.ofMinutes(25), Duration.between(start, instants.get(4)));
        assertEquals(Duration.ofMinutes(25), Duration.between(start, instants.get(5)));
        assertEquals(Duration.ofMinutes(35), Duration.between(start, instants.get(6)));
        assertEquals(Duration.ofMinutes(35), Duration.between(start, instants.get(7)));
        assertEquals(Duration.ofMinutes(35), Duration.between(start, instants.get(8)));
        assertEquals(Duration.ofMinutes(35), Duration.between(start, instants.get(9)));

        // Check cancelling
        assertTrue(future.isDone());
        assertFalse(future.cancel(false));
    }

    @Test
    public void shutdownNow() {
        final AtomicInteger counter = new AtomicInteger(0);

        executorService.schedule((Runnable) () -> counter.incrementAndGet(), 1, TimeUnit.MINUTES);
        executorService.schedule(() -> counter.incrementAndGet(), 1, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(() -> counter.incrementAndGet(), 1, 1, TimeUnit.MINUTES);
        executorService.scheduleWithFixedDelay(() -> counter.incrementAndGet(), 1, 1, TimeUnit.MINUTES);

        final List<Runnable> runnables = executorService.shutdownNow();
        for (final Runnable runnable : runnables) {
            runnable.run();
        }

        assertEquals(4, counter.get());
    }
}
