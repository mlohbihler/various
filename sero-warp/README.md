A neat little piece that is able to simulate time, which is very handy for testing code that contains sleeps, waits,
or stuff scheduled in a ScheduledExecutorService. There are some client code changes that are required to use this, including:

* Change calls to Object.wait(timeout) to WarpUtils.wait()
* Change calls to Thread.sleep(timeout) to WarpUtils.sleep()
* Instead of using a ScheduledExecutorService, use a WarpScheduledExecutorService
* Inject a Clock instance into your code.

In your client code these changes will make no functional difference (and negligible performance difference). In your test code, you will need to create a WarpClock and inject it into the code that you want to test. The use of the WarpClock indicates that you want to simulate time. Thereafter, you will call the WarpClock.plus() methods to advance the simulated time.

See the included unit tests for usage examples.

Copyright (c) 2017, Matthew Lohbihler
