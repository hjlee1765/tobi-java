package me.hjlee.reactive;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class HjleeApplication {
    @RestController
    public static class Controller {
        //스프링에 reactive를 적용할 때에는 publisher만 만들면 된다.
        //subscriber는 스프링이 원하는 방식으로 원하는 시점에 만들어서 publisher에게 데이터를 요청에서 받는다.
        //Controller는 publisher를 리턴하고, subscriber가 subscribe를 해야지 실행이 되는데 이걸 스프링 MVC가 한다.

        @RequestMapping("/hello")
        public Publisher<String> hello(String name) {
            return new Publisher<String>() {
                @Override
                public void subscribe(Subscriber<? super String> s) {
                    s.onSubscribe(new Subscription() {
                        //spring 이, 즉 sub이 request를 호출할 것이다.
                        @Override
                        public void request(long n) {
                            s.onNext("Hello " + name);
                            s.onComplete();
                        }
                        @Override
                        public void cancel() {
                        }
                    });
                }
            };
        }
        public static void main(String[] args) {
            SpringApplication.run(HjleeApplication.class, args);
        }
    }
}
