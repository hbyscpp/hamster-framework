package com.seaky.hamster.core.test;

import java.util.List;

public interface TestService {

  long sub(long x, Short y);

  String hello(String h);

  String concat(TestDto dto);

  String ping();

  int add(int x, int y);

  void testVoid();

  void testException();

  void testCircuitbreaker(int time);

  List<TestDto> getAll();

}
