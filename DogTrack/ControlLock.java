package DogTrack;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ControlLock {
    private boolean paused = false;
    private final Lock lock = new ReentrantLock();
    private final Condition unpaused = lock.newCondition();

    

    public void pause() {
       lock.lock();
        try {
            paused = true;
        } finally {
            lock.unlock();
        }
        
    }

    public void resume() {
        lock.lock();
        try {
            paused = false;
            unpaused.signalAll();
        } finally {
            lock.unlock();
        }
    }

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
