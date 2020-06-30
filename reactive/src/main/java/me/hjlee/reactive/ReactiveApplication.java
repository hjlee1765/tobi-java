package me.hjlee.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLOutput;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ReactiveApplication {
    //Observable(Publisher) : 새로운 정보가 발생 하면 Event 를 Observer 에게 던진다.
    static class IntObservable extends Observable implements Runnable{
        @Override
        public void run() {
            for(int i=1; i<=10; i++){
                setChanged();
                notifyObservers(i);         //push
                //int i = it.next();        //pull
            }
        }
    }
    public static void main(String[] args) {
        Observer ob = new Observer(){
            //notify 가 발생 하면 실행 된다.
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };
        //Observable(publisher)에 Observer(subscriber)를 등록한다.
        IntObservable io = new IntObservable();
        io.addObserver(ob);

        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + "  EXIT");
        es.shutdown();
    }

/*        //인터페이스 구현. 메서드 1개일 때, 람다식 사용
        Iterable<Integer> iter = () ->
            new Iterator<Integer>() {
                int i= 10;
                final static int MAX = 20;

                @Override
                public boolean hasNext() {
                    return i < MAX;
                }

                @Override
                public Integer next() {
                    return ++i;
                }
            };
        //결론 : 자바의 for-each문은 collection이 아닌 iterable을 구현한 오브젝트를 넣는다.
        for(Integer i : iter){
            System.out.println(i);
        }*/

}
