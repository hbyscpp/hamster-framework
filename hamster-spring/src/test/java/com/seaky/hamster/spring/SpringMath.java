package com.seaky.hamster.spring;

import org.springframework.stereotype.Service;

@Service("localmath")
public class SpringMath implements Math {

  @Override
  public int add(int x, int y) {
    return x + y;
  }

  @Override
  public int sub(int x, int y) {
    // TODO Auto-generated method stub
    return x - y;
  }

  @Override
  public void delay(long time) {

    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


}
