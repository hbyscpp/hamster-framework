package com.seaky.hamster.core.benchmark;

public class FixSizeResponsebenchmarkTestService implements BenchmarkTestService{

	private int size;
	
	public FixSizeResponsebenchmarkTestService(int size)
	{
		this.size=size;
	}

	@Override
	public ResponseObject callMe(RequestObject request) {
		ResponseObject rsp=new ResponseObject();
		rsp.setData(new byte[size]);
		return rsp;
	}


}
