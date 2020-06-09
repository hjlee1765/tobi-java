package me.hjlee.generics;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class GenericsApplication{

    //comparable 인터페이스를 구현한 타입만 T로 받겠다.
    //filter(s-> s.compare)를 사용하기 위해 bounded type parameter를 사용한다.
    static <T extends Comparable<T>> long countGreaterThan(T[] arr, T elem){
        return Arrays.stream(arr).filter(s -> s.compareTo(elem) > 0).count();
    }

    public static void main(String[] args) {
        //Integer[] arr = new Integer[] {1,2,3,4,5,6,7};
        String[] arr = new String[] {"a","b","c","d","e","f","g"};
        System.out.println(countGreaterThan(arr,"b"));
    }
}
