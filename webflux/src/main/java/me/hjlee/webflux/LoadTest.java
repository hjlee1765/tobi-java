package me.hjlee.webflux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTest {
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        Logger logger = LoggerFactory.getLogger(LoadTest.class);
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8080/rest?idx={idx}";

        //스레드의 동기화. parties의 개수만큼
        //await을 만나는 순간, parties의 숫자에 도달하기 전까지 스레드는 blocking된다.
        CyclicBarrier barrier = new CyclicBarrier(101);

        for (int i = 0; i < 100; i++) {
            //execute는 runnerble. runnerble은 exception을 던질 수 없음.
            //따라서 callable로 바꾼다.(리턴값이 있고 exception을 던질 수 있다)
            es.submit(() -> {
                int idx = counter.addAndGet(1);

                barrier.await();    //parties의 숫자에 도달하면 모든 스레드가 동시에 실행됨.

                logger.info("Thread {}",idx);
                StopWatch sw = new StopWatch();
                sw.start();

                String res = rt.getForObject(url, String.class, idx); //getForObject : blocking method. 대기상태에 빠진다.

                sw.stop();
                logger.info("Elapsed: {} {} / {}", idx, sw.getTotalTimeSeconds(), res);

                return null;
            });
        }

        barrier.await();    //parties를 101개로 한 이유.
        StopWatch main = new StopWatch();
        main.start();

        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);

        main.stop();
        logger.info("Total: {}", main.getTotalTimeSeconds());
    }
}
