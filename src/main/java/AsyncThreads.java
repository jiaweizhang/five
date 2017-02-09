import com.google.inject.Inject;
import services.KeyValueService;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by jiaweizhang on 2/8/2017.
 */
public class AsyncThreads {

    private final int MAX_SLEEP_TIME_IN_MILLISECONDS = 60000;
    private final int MIN_SLEEP_TIME_IN_MILLISECONDS = 100;
    // number of consecutive times no files to delete were found
    private int waitTimeCalculationState = 0;

    private KeyValueService keyValueService;

    @Inject
    public AsyncThreads(KeyValueService keyValueService) {
        this.keyValueService = keyValueService;
    }

    /**
     * Start asynchronous threads that delete files
     */
    public void start() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        // lock used for state used to calculate wait time
        ReadWriteLock waitTimeCalculationStateLock = new ReentrantReadWriteLock();

        // note that multiple Callable can be executing at a time
        Callable<Integer> cleanFilesTask = () -> {
            try {
                // find all expired URLs to process
                Set<String> newUrlsToDelete = keyValueService.findExpiredUrlsIfExist();
                for (String url : newUrlsToDelete) {
                    // actually delete files

                    // mark as deleted
                    keyValueService.deleteUrl(url);
                }

                boolean found = !newUrlsToDelete.isEmpty();

                // acquire write lock that processes currentSleepTimeInSeconds?
                waitTimeCalculationStateLock.writeLock().lock();
                // process waitTimeCalculationState depending on whether URL was found
                try {
                    if (found) {
                        waitTimeCalculationState = 0;
                    } else {
                        waitTimeCalculationState += 1;
                    }
                } finally {
                    // release lock
                    waitTimeCalculationStateLock.writeLock().unlock();
                }

                // acquire read lock
                // TODO can simply save variable during write stage and use later
                waitTimeCalculationStateLock.readLock().lock();
                // calculate time to wait for next thread
                int waitTime;
                try {
                    // always wait at least 100 milliseconds so more important URL creation and accesses can run
                    // perform exponential scaling depending on uses up until max
                    // TODO ensure no overflow occurs
                    waitTime = (int) Math.min(Math.pow(1.5, waitTimeCalculationState) * MIN_SLEEP_TIME_IN_MILLISECONDS, MAX_SLEEP_TIME_IN_MILLISECONDS);
                } finally {
                    // release lock
                    waitTimeCalculationStateLock.readLock().unlock();
                }

                // return wait time
                return waitTime;
            } catch (Exception e) {
                throw new IllegalStateException("Poll task interrupted", e);
            }
        };

        // schedule first execution to run within min sleep time
        ScheduledFuture<Integer> waitTimeFuture = executor.schedule(cleanFilesTask, MIN_SLEEP_TIME_IN_MILLISECONDS, TimeUnit.MILLISECONDS);

        while (true) {
            try {
                int waitTime = waitTimeFuture.get();
                // schedule subsequent executions to run within designated wait time
                waitTimeFuture = executor.schedule(cleanFilesTask, waitTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Wait interrupted", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Wait failed", e);
            }
        }
    }
}
