package com.seaky.hamster.core.test;

public class MathImpl1 implements Math {

  @Override
  public int add(int x, int y) {

    return x + y;
  }

  @Override
  public int sub(int x, int y) {
    return sub1(x, y);
  }

  @Override
  public String hello(String h) {
    return null;
  }

  private int sub1(int x, int y) {
    throw new RuntimeException("not support");

  }

}
