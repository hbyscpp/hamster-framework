package com.seaky.hamster.core.test;


public class MathImpl2 implements Math {


	@Override
	public int add(int x, int y) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int sub(int x, int y) {
		return x-y;
	}

	@Override
	public String hello(String h) {
		return h;
	}

}
