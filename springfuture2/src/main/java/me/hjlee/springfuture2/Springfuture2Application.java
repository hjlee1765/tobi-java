package me.hjlee.springfuture2;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootApplication
@RestController
public class Springfuture2Application {

    static final String URL1 = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";

    AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

    @Autowired
    MyService myService;

    @GetMapping("/rest")
    public DeferredResult<String> rest(int idx) {
        DeferredResult<String> dr = new DeferredResult<>();

        toCF(rt.getForEntity(URL1, String.class, "h" + idx))
                .thenCompose(s -> toCF(rt.getForEntity(URL2, String.class, s.getBody())))
                .thenApplyAsync(s2 -> myService.work(s2.getBody()))
                .thenAccept(s3 -> dr.setResult(s3))
                .exceptionally(e -> {dr.setErrorResult(e.getMessage()); return (Void)null;});

/*            Completion
                .from(rt.getForEntity(URL1, String.class, "h" + idx))
                .andApply(s -> rt.getForEntity(URL2, String.class, s.getBody()))
                .andError(e -> dr.setErrorResult(e))    //에러 발생시 종료. 에러 없을 경우 아래 직렬화 함수 수행.
                .andAccept(s -> dr.setResult(s.getBody()));*/


        return dr;
    }
    //lf -> to CF
    <T> CompletableFuture<T> toCF(ListenableFuture<T> lf){
        CompletableFuture<T> cf = new CompletableFuture<T>();
        lf.addCallback(s -> cf.complete(s), e-> cf.completeExceptionally(e));
        return cf;
    }

    public static void main(String[] args) {
        SpringApplication.run(Springfuture2Application.class, args);
    }

    public static class AcceptCompletion extends Completion{
        //세 번째 Completion 인스턴스에 저장될 값.
        public Consumer<ResponseEntity<String>> con;
        public AcceptCompletion(Consumer<ResponseEntity<String>> con) {
            this.con = con;
        }

        @Override
        void run(ResponseEntity<String> value) {
            con.accept(value);
        }
    }

    public static class ErrorCompletion extends Completion{
        public Consumer<Throwable> econ;
        public ErrorCompletion(Consumer<Throwable> econ) {
            this.econ = econ;
        }

        @Override
        void run(ResponseEntity<String> value) {
            if(next != null) next.run(value);
        }

        @Override
        void error(Throwable e) {
            econ.accept(e);
        }
    }

    public static class ApplyCompletion extends Completion{
        //두 번째 Completion인스턴스에 저장될 값.
        public Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn;
        public ApplyCompletion(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn) {
            this.fn = fn;
        }

        @Override
        void run(ResponseEntity<String> value) {
            ListenableFuture<ResponseEntity<String>> lf = fn.apply(value);
            lf.addCallback(s->complete(s), e->error(e));
        }
    }

    //인스턴스 변수인 next와 con은 이 class가 인스턴스 생성될 때 만들어진다.
    public static class Completion {
        //인스턴스 끼리 엮어주는 next.
        Completion next;

        //세 번째 Completion 인스턴스를 생성 후 con 저장, 두 번째 Completion 인스턴스의 next에 등록
        //넘어오는 람다식 함수를 두 번째 인스턴스의 con에 등록.
        public void andAccept(Consumer<ResponseEntity<String>> con){
            Completion c = new AcceptCompletion(con);
            this.next = c;
        }

        //에러가 있으면 아래 단계 실행 안되고 바로 종료.
        //에러가 없으면 아래 단계(andAccept)가 수행 되어야되니, Completion을 리턴.
        public Completion andError(Consumer<Throwable> econ){
            Completion c = new ErrorCompletion(econ);
            this.next = c;
            return c;
        }

        //두 번째 Completion 인스턴스를 생성 후 fn 저장, 첫 번째 Completion 인스턴스의 next에 등록
        public Completion andApply(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn){
            Completion c = new ApplyCompletion(fn);
            this.next = c;
            return c;
        }
        //최초 Completion 인스턴스 생성 및 첫 번째 비동기 작업의 callback을 등록.
        public static Completion from(ListenableFuture<ResponseEntity<String>> lf){
            Completion c = new Completion();
            lf.addCallback(s->{
                c.complete(s);
            }, e->{
                c.error(e);
            });
            return c;
        }
        void error(Throwable e) {
            if (next != null) next.error(e);
        }
        void complete(ResponseEntity<String> s) {
            if(next != null) next.run(s);
        }
        void run(ResponseEntity<String> value) {
        }
    }
    @Service
    public static class MyService {
        public String work(String req){
            return req + "/asyncwork";
        }
    }

    @Bean
    public ThreadPoolTaskExecutor myThreadPool(){
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(1);
        te.setMaxPoolSize(1);
        te.initialize();
        return te;
    }
}
