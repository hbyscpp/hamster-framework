package com.seaky.hamster.core.test;

import java.util.concurrent.Future;

public interface MathAsyn {

	Future<Integer> add(int x, int y);

	Future<Integer> sub(int x, int y);

	Future<String> hello(String h);
}
