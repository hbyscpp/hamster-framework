package com.seaky.hamster.core.test;

import rx.Observable;

public interface TestServiceReactive {

  Observable<Long> sub(long x, Short y);

  Observable<String> hello(String h);

  Observable<String> concat(TestDto dto);

  Observable<String> ping();

  Observable<Integer> add(int x, int y);

  Observable<Void> testVoid();

  Observable<Void> testException();

  Observable<Void> testCircuitbreaker(int time);
}
