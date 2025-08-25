package PrimeFinder;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * This class provides a mechanism to pause and resume threads safely.
 * It uses a Lock and Condition to manage the paused state.
 */
public class PauseControl {
    private boolean paused = false;
    private final Lock lock = new ReentrantLock();
    private final Condition unpaused = lock.newCondition();

    /*
     * Pauses the thread by setting the paused flag to true.
     */
    public void pause() {
        lock.lock();
        try {
            paused = true;
        } finally {
            lock.unlock();
        }
    }

    /*
     * Resumes the thread by setting the paused flag to false and signaling all waiting threads.
     */
    public void resume() {
        lock.lock();
        try {
            paused = false;
            unpaused.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /*
     * Checks if the thread is paused and waits if it is.
     * This method should be called periodically in the thread's run method.
     */
    public void checkPaused() throws InterruptedException {
        lock.lock();
        try {
            while (paused) {
                unpaused.await();
            }
        } finally {
            lock.unlock();
        }
    }

}
