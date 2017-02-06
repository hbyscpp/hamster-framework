package com.seaky.hamster.core.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

public class TestServiceImpl implements TestService {

  @Override
  public long sub(long x, Short y) {
    return x - y;
  }

  @Override
  public String hello(String h) {
    return "hello" + h;
  }

  @Override
  public String concat(TestDto dto) {
    List<Object> datas = dto.getDatas();
    StringBuilder sb = new StringBuilder();

    for (Object o : datas) {
      sb.append(o);
    }
    return sb.toString();
  }

  @Override
  public String ping() {
    return "ping";
  }

  @Override
  public int add(int x, int y) {
    return x + y;
  }

  @Override
  public void testVoid() {

  }

  @Override
  public void testException() {
    throw new NotImplementedException();
  }


  @Override
  public void testCircuitbreaker(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<TestDto> getAll() {
    List<TestDto> all = new ArrayList<>();
    all.add(new TestDto());
    all.add(new TestDto());
    return all;
  }

}
