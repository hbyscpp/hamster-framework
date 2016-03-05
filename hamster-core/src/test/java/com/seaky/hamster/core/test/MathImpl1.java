package com.seaky.hamster.core.test;

public class MathImpl1 implements Math {

	@Override
	public int add(int x, int y) {
	
		try {
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return x + y;
	}

	@Override
	public int sub(int x, int y) {
		return x - y;
	}

	@Override
	public String hello(String h) {
		return h;
	}

}
