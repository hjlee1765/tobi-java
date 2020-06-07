package me.hjlee.springfuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.*;

@SpringBootApplication
@Slf4j
@EnableAsync
public class SpringfutureApplication {

    //Callable 을 사용하면, 서블릿 스레드는 바로 반환시키고 백단의 워커스레드에서 작업을 돌린다.
    //워커 스레드가 작업종료되면 새로운 서블릿 스레드가 받아서 리턴시킨다.
    @RestController
    public static class MyController{
        @GetMapping("/callable")
        public Callable<String> async() throws InterruptedException {
            log.info("callable");
            return () -> {
                log.info("async");
                Thread.sleep(5000);
                return "hello";
            };
        }
    }


    /*

    //메인쓰레드와 비동기 워킹스레드 중 어느것이 먼저 끝나는지에 대한 테스트
    @Component
    public class AsyncService{
        @Async
        public ListenableFuture<String> asyncTestMethod() throws InterruptedException {
            Thread.sleep(2000);
            return new AsyncResult<>("done");
        }
    }

    @RestController
    public static class AsyncController{

        @Autowired
        AsyncService asyncService;

        @GetMapping("/asyncTest")
        public String asyncTest() throws InterruptedException {
            ListenableFuture<String> f = asyncService.asyncTestMethod();
            f.addCallback(s -> System.out.println(s), e -> System.out.println("Error: " + e.getMessage()));
            return "async";
        }
    }
*/



/*
    //deferred Result 예제
    @RestController
    public static class DeferrredResultController{
        Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();

        @GetMapping("/dr")
        public DeferredResult<String> async() throws InterruptedException {
            log.info("dr");
            DeferredResult<String> dr = new DeferredResult<>();
            results.add(dr);
            return dr;
        }
        @GetMapping("/dr/count")
        public String drcount(){
            return String.valueOf(results.size());
        }
        @GetMapping("/dr/event")
        public String drevent(String msg){
            for(DeferredResult<String> dr : results){
                dr.setResult("Hello " + msg);
                results.remove(dr);
            }
            return "OK";
        }

    }*/

/*
    //emitter 예제
    //한번 요청에, 데이터를 나눠서 여러번 응답을 보낼 수 있다.
    @RestController
    public static class DeferrredResultController {
        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter() {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();

            Executors.newSingleThreadExecutor().submit(() -> {
                for (int i = 1; i <= 50; i++) {
                    try {
                        emitter.send("<p>Stream " + i + "</p>");
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
            });
            return emitter;
        }
    }*/

    public static void main(String[] args) {
       SpringApplication.run(SpringfutureApplication.class, args);
    }
}
