package me.hjlee.reactive;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class PubSub {
    public static void main(String[] args) throws InterruptedException {

        //
        // pub -> [Data1] -> mapPub -> [Data2] -> LogSub
        //                <- subscribe(LogSub)          //pub 에 정의된 메서드를 실행.    (Subscriber를 오버라이드 하면서 최초 pub까지 넘겨준다.)
        //                -> onSubscribe(Subscription)  //sub에 정의된 메서드를 pub이 실행 (최초 pub이 실행, Subscription 객체 생성.)
        //                <- request(unbounded)         //최초 pub에 정의된 Subscription 객체의 Request 메서드를 sub이 실행
        //                -> onNext(iter)               //첫 번째 pub에서 지금껏 오버라이드 된 Subscriber의 onNext함수를 실행.
        //                -> onNext                     //두 번째 pub에서 지금껏 오버라이드 된 Subscriber의 onNext함수를 실행.
        //                -> onNext
        //                -> onComplete                 //sub에 정의된 메서드를 pub이 실행

        Publisher<Integer> pub = iterPub(Stream.iterate(1, a->a+1).limit(10).collect(Collectors.toList()));
        //Publisher<String> mapPub = mapPub(pub, s -> "[" + s + "]"); //타입 변환 가능한 mapPub
        //Publisher<Integer> sumPub = sumPub(pub);

        // 1,2,3,4,5
        // 0 -> (0,1) -> 0 + 1 = 1
        // 1 -> (1,2) -> 1 + 2 = 3
        // 3 -> (3,3) -> 3 + 3 = 6
        Publisher<StringBuilder> reducePub = reducePub(pub, new StringBuilder (),
                (BiFunction<StringBuilder, Integer, StringBuilder>)(a, b)-> a.append(b+","));

        //mapPub.subscribe(logSub());
        reducePub.subscribe(logSub());
    }

    private static <T, R> Publisher<R> reducePub(Publisher<T> pub, R init, BiFunction<R, T, R> bf) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub<T, R>(sub){
                    R result = init;

                    @Override
                    public void onNext(T i) {
                        //reduce구현 -> input : 현재의 결과값, 다음 리스트 값.
                        result = bf.apply(result, i);
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }

/*    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub){
                    int sum = 0;

                    @Override
                    public void onNext(Integer i) {
                        sum += i;
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(sum);
                        sub.onComplete();
                    }
                });
            }
        };
    }*/

    //T -> R 변환.
    private static <T,R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub ) {
                pub.subscribe(new DelegateSub<T,R>(sub) {
                    @Override
                    public void onNext(T i) {
                        sub.onNext(f.apply(i));
                    }
                });
            }
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe");
               s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T i) {
                log.debug("onNext:{}", i);
            }

            @Override
            public void onError(Throwable t) {
                log.debug("onError:{}", t);
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        };
    }

    private static Publisher<Integer> iterPub(List<Integer> iter) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        try{
                            iter.forEach(s->sub.onNext(s));
                            sub.onComplete();      //publisher가 끝났다고 하는 신호를 보낸다.
                        }
                        catch (Throwable t){
                            sub.onError(t);
                        }
                    }
                    @Override
                    public void cancel() {
                    }
                });
            }
        };
    }
}
