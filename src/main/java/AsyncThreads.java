import com.google.inject.Inject;
import services.KeyValueService;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by jiaweizhang on 2/8/2017.
 */
public class AsyncThreads {

    private final int MAX_SLEEP_TIME_IN_MILLISECONDS = 60000;
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
                // perform work

                boolean found = false;

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
                    waitTime = (int) Math.min(Math.pow(1.5, waitTimeCalculationState) * 100, MAX_SLEEP_TIME_IN_MILLISECONDS);
                } finally {
                    // release lock
                    waitTimeCalculationStateLock.readLock().unlock();
                }

                // return time to wait for Callable to execute again
                return waitTime;
            } catch (Exception e) {
                throw new IllegalStateException("Poll task interrupted", e);
            }
        };


    }
}
