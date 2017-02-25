package com.seaky.hamster.core.test;

import java.util.List;
import java.util.concurrent.Future;

public interface TestServiceFuture {


  Future<Long> sub(long x, Short y);

  Future<String> hello(String h);

  Future<String> concat(TestDto dto);

  Future<String> ping();

  Future<Integer> add(int x, int y);

  Future<Void> testVoid();

  Future<Void> testException();

  Future<Void> testCircuitbreaker(int time);

  Future<List<TestDto>> getAll();
}
