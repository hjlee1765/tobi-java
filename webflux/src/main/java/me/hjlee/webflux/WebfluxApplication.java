package me.hjlee.webflux;

import ch.qos.logback.core.net.server.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@SpringBootApplication
@RestController
public class WebfluxApplication {

    static final String URL1 = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";

    @Autowired
    MyService myService;

    WebClient client = WebClient.create();

    //Mono를 리턴하면 spring webflux flamwork가 subscriber를 호출해서, 퍼블리셔가 동작하기 시작한다.
    @GetMapping("/rest")
    public Mono<String> rest(int idx) {
        return client.get().uri(URL1, idx).exchange()                       //Mono<ClientResponse>
                .flatMap(c -> c.bodyToMono(String.class))                   //Mono<String>
                .flatMap(res1 -> client.get().uri(URL2, res1).exchange())   //Mono<ClientResponse>
                .flatMap(c -> c.bodyToMono(String.class))                   //Mono<String>
                // 동기 함수 호출 시.
                //.map(res2 -> myService.work(res2));

                // (작업이 오래걸려) 비동기 함수 호출 시.
                //completableFuture<> -> Mono로 변환 후 flatmap 사용.
                .flatMap(res2 -> Mono.fromCompletionStage(myService.workAsync(res2)));
    }

    public static void main(String[] args) {
        System.setProperty("reactor.ipc.netty.workerCount", "1");
        System.setProperty("reactor.ipc.netty.pool.maxConnections", "2000");
        SpringApplication.run(WebfluxApplication.class, args);
    }

    @Service
    public static class MyService{
        public String work(String req){
            return req + "/asyncwork";
        }
        @Async
        public CompletableFuture<String> workAsync(String req){
            return CompletableFuture.completedFuture(req + "/asyncwork");
        }
    }

}
