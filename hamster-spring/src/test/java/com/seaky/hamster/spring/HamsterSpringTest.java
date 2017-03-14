package com.seaky.hamster.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {SpringMath.class, SpringInterceptorService.class, HamsterConfig.class,
        HamsterExportServiceConfig.class, HamsterReferConfig.class},
    loader = AnnotationConfigContextLoader.class)
public class HamsterSpringTest {

  @Autowired
  @Qualifier("remotemath")
  private Math math;

  @Test
  public void testMath() {

    long begin = System.currentTimeMillis();
    try {
      math.delay(120 * 1000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    long end = System.currentTimeMillis();
    System.out.println("call time " + (end - begin));
    // System.out.println(math.add(1, 2));
    // System.out.println(math.sub(1, 2));
  }

}
