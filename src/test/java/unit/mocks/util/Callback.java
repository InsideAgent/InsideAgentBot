package unit.mocks.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Callback<T> {

    private final CountDownLatch countDownLatch;
    private T object;

    public Callback() {
        this.countDownLatch = new CountDownLatch(1);
    }

    public void callback(T object) {
        this.object = object;
        this.countDownLatch.countDown();
    }

    public T await() throws InterruptedException {
        this.countDownLatch.await();
        return object;
    }

    public T await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        if (!this.countDownLatch.await(timeout, timeUnit)) {
            throw new InterruptedException("Timeout elapsed");
        }
        return object;
    }

}
