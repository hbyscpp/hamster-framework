package com.seaky.hamster.core.test;

public class MathImpl1 implements Math {

  @Override
  public int add(int x, int y) {

    return x + y;
  }

  @Override
  public int sub(int x, int y) {
    throw new RuntimeException("not support");
  }

  @Override
  public String hello(String h) {
    return null;
  }

}
