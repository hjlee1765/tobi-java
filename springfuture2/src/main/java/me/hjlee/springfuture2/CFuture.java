package me.hjlee.springfuture2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.concurrent.*;

public class CFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException{
        Logger logger = LoggerFactory.getLogger(CFuture.class);
        ExecutorService es = Executors.newFixedThreadPool(10);

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info("runAsync");
                    return 1;
                })
                .thenCompose(s -> {
                    logger.info("thenCompose {}", s);
                    return CompletableFuture.completedFuture(s+1);
                })
                .thenApply(s2 -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info("thenApply {}", s2);
                    return s2 * 3;
                })
                .exceptionally(e -> -10)
                .thenAccept(s3 -> logger.info("thenAccept {}", s3));

        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }
}
