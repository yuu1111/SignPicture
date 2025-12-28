package net.teamfruit.signpic.http;

import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.config.SignPicConfig;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages HTTP communication tasks with a thread pool.
 */
public class Communicator {
    private static Communicator instance;

    public static Communicator getInstance() {
        if (instance == null) {
            instance = new Communicator();
        }
        return instance;
    }

    private final Queue<CommunicateTask> tasks = new ConcurrentLinkedQueue<>();
    private ExecutorService threadPool;

    private Communicator() {
        initThreadPool();
    }

    private void initThreadPool() {
        int threads = SignPicConfig.get().httpThreads;
        AtomicInteger threadCounter = new AtomicInteger(0);
        this.threadPool = new ThreadPoolExecutor(
                0, threads,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r, "signpic-http-" + threadCounter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
        );
    }

    public Queue<CommunicateTask> getTasks() {
        return tasks;
    }

    public void submit(CommunicateTask task) {
        tasks.offer(task);
        threadPool.execute(() -> {
            try {
                task.execute();
            } catch (Exception e) {
                SignPicture.LOGGER.error("HTTP task failed", e);
                task.onError(e);
            } finally {
                tasks.remove(task);
                task.onComplete();
            }
        });
    }

    public void shutdown() {
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
    }

    /**
     * Represents a single communication task.
     */
    public interface CommunicateTask {
        void execute() throws Exception;

        default void onComplete() {}

        default void onError(Exception e) {}
    }
}
