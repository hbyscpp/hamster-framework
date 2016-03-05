package com.seaky.hamster.core.test;

import rx.Observable;

public interface MathReactive {
	
	Observable<Integer> add(int x, int y);

	Observable<Integer> sub(int x, int y);
	
	Observable<String> hello(String h);
}
