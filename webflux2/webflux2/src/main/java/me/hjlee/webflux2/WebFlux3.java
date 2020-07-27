package me.hjlee.webflux2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
@RestController
public class WebFlux3 {

    @GetMapping("/event/{id}")
    Mono<List<Event>> event(@PathVariable long id){
        List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
        return Mono.just(list);
    }

    //소스에서 데이터 생성
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> eventsSourceGen(){
        Stream<Event> s = Stream.generate(() -> new Event(System.currentTimeMillis(), "value"));

        return Flux
                .fromStream(s)                              //stream
                .delayElements(Duration.ofSeconds(1))       //1초에 한번씩 데이터를 return한다.
                .take(10);                                  //pub에 있는 데이터 10개를 가져온다.
    }

    //flux 의 오퍼레이터를 이용하여 publisher 데이터 생성
    @GetMapping(value = "/eventsFluxGen", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> eventsFluxGen(){
        return Flux
                .<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "value")))
                .delayElements(Duration.ofSeconds(1))       //1초에 한번씩 데이터를 return한다.
                .take(10);                                  //pub에 있는 데이터 10개를 가져온다.
    }

    @GetMapping(value = "/eventsFluxGen2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> eventsFluxGen2(){
        return Flux
                .<Event, Long>generate(()->1L, (id, sink)->{
                    sink.next(new Event(id,"value" + id));
                    return id+1;
                })
                .delayElements(Duration.ofSeconds(1))       //1초에 한번씩 데이터를 return한다.
                .take(10);                                  //pub에 있는 데이터 10개를 가져온다.
    }

    //Flux.interval 사용.
    //Flux에서 데이터를 생성하는데, 일정한 주기를 가지고 0부터 생성하는 방식.
    //두 개의 flux를 머지 가능함.
    @GetMapping(value = "/eventsFluxGen3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> eventsFluxGen3(){

        Flux<Event> es = Flux.<Event, Long>generate(()-> 1L, (id, sink) ->{
                    sink.next(new Event(id, "value"+ id));
                    return id + 1;
                });

        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
        //두 개의 Flux를 투플로 묶는다.
        return Flux.zip(es, interval).map(tuple -> tuple.getT1());
    }

    public static void main(String[] args)  {SpringApplication.run(Webflux2Application.class, args);}

    @Data @AllArgsConstructor
    public static class Event{
        long id;
        String value;
    }
}
