package me.hjlee.reactive2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.GsonBuilderUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FluxScEx {
    public static void main(String[] args) throws InterruptedException {

            Flux.interval(Duration.ofMillis(200))
                    .take(10)
                    .subscribe(s->log.debug("onNext:{}", s));

                    log.debug("exit");
                    TimeUnit.SECONDS.sleep(10);
    }
}




/*
        Flux.range(1, 10)
                .publishOn(Schedulers.newSingle("pub"))
                .log()
                // .subscribeOn(Schedulers.newSingle("sub"))
                .subscribe(s-> System.out.println(s));

                //스레드 풀이 만들어지면 종료시키기 전까지 프로그램이 끝나지 않는다.
                System.out.println("exit");*/
