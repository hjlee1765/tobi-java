package me.hjlee.reactive2;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SchedulerEx {
    public static void main(String[] args) {
        Publisher<Integer> pub = sub -> {
            sub.onSubscribe(new Subscription() {
                @Override
                public void request(long l) {
                    log.debug("request()");
                    sub.onNext(1);
                    sub.onNext(2);
                    sub.onNext(3);
                    sub.onNext(4);
                    sub.onNext(5);
                    sub.onComplete();
                }
                @Override
                public void cancel() {
                }
            });
        };

/*
       //subscribeOn
        Publisher<Integer> subOnPub = sub -> {
            //max thread pool = 1, 그 이상 작업이 오면  Queuing
            //CustomizableThreadFactory ->  손쉽게 스레드 팩토리를 만들수 있는 클래스.
            ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory(){
                //스레드 이름을 변경해서 스레드를 생성한다.
                @Override
                public String getThreadNamePrefix() {
                    return "subOn-";
                }
            });
            es.execute(()->pub.subscribe(sub));
        };
*/

        //publishOn
        Publisher<Integer> pubOnPub = sub -> {
            pub.subscribe(new Subscriber<Integer>() {
                ExecutorService es = Executors.newSingleThreadExecutor(new CustomizableThreadFactory(){
                    @Override
                    public String getThreadNamePrefix() {
                        return "pubOn-";
                    }
                });

                @Override
                public void onSubscribe(Subscription s) {
                    sub.onSubscribe(s);
                }

                @Override
                public void onNext(Integer integer) {
                    es.execute(()->sub.onNext(integer));
                }

                @Override
                public void onError(Throwable t) {
                    es.execute(()->sub.onError(t));
                    es.shutdown();
                }

                @Override
                public void onComplete() {
                    //스레드 풀이 만들어지면, 종료시키기 전까지 프로그램이 끝나지 않는다.
                    es.execute(()->sub.onComplete());
                    es.shutdown();
                }
            });
        };

        //sub
        pubOnPub.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                log.debug("onNext:{}", integer);
            }

            @Override
            public void onError(Throwable t) {
                log.debug("onError:{}", t);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        });
        System.out.println("exit");
    }
}
