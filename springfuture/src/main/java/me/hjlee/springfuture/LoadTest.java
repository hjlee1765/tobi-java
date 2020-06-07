package me.hjlee.springfuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTest {
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8080/callable";

        StopWatch main = new StopWatch();
        main.start();

        for (int i = 0; i < 100; i++) {
            //i는 final이 아니다.값이 계속 변한다.다른 스레드에서 또 다른 스레드에 로컬에 정의된 변수값을 직접 접근 불가능하다.
            //execute는 작업 내용 결과를 받지 않을 때 사용한다. (submit과 return의 유/무에서 다름)
            //메인 쓰레드가 워킹 쓰레드보다 먼저 종료되도 관계가 없느건가?
            es.execute(() -> {
                int idx = counter.addAndGet(1);
                log.info("Thread " + idx);

                StopWatch sw = new StopWatch();
                sw.start();

                rt.getForObject(url, String.class);
                sw.stop();
                log.info("Elapsed: {} {}", idx, sw.getTotalTimeSeconds());
            });
        }
        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);
        main.stop();
        log.info("Total: {}", main.getTotalTimeSeconds());
    }
}
