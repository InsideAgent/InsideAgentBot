package dev.jacrispys.JavaBot.api.libs.utils.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public abstract class AsyncHandlerImpl implements AsyncHandler{

    public record VoidMethodRunner(Runnable runnable, CompletableFuture<Void> cf) {}
    public record MethodRunner(Runnable runnable, CompletableFuture<?> cf) {}

    public final BlockingQueue<MethodRunner> methodQueue = new ArrayBlockingQueue<>(5);
    public final BlockingQueue<VoidMethodRunner> voidMethodQueue = new ArrayBlockingQueue<>(5);

    public void completeVoid() {
        try {
            for (; ; ) {
                VoidMethodRunner runner = voidMethodQueue.take();
                runner.runnable.run();
                runner.cf().complete(null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void completeMethod() {
        try {
            for (; ;) {
                MethodRunner runner = methodQueue.take();
                runner.runnable.run();
                while (true) {
                    if (!runner.cf.isDone() && !runner.cf.isCancelled()) continue;
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
