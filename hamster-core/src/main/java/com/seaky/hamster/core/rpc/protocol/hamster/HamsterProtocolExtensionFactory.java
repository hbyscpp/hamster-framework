package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.annotations.SPI;
import com.seaky.hamster.core.rpc.protocol.ProtocolExtensionFactory;
import com.seaky.hamster.core.rpc.protocol.RequestConvertor;
import com.seaky.hamster.core.rpc.protocol.RequestInfoExtractor;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;
import com.seaky.hamster.core.rpc.protocol.ResponseInfoExtractor;

@SPI("hamster")
public class HamsterProtocolExtensionFactory implements
		ProtocolExtensionFactory<HamsterRequest, HamsterResponse> {
	private static HamsterResponseInfoExtractor repextractor = new HamsterResponseInfoExtractor();
	private static HamsterRequestInfoExtractor extractor = new HamsterRequestInfoExtractor();
	private static HamsterRequestAttrWriter hamsterRequestAttrWriter = new HamsterRequestAttrWriter();
	private static HamsterResponseAttrWriter hamsterResponseAttrWriter = new HamsterResponseAttrWriter();

	public HamsterServer createServer() {
		HamsterServer server = new HamsterServer();
		return server;
	}

	@Override
	public RequestInfoExtractor<HamsterRequest> getRequestInfoExtractor() {
		return extractor;
	}

	@Override
	public Class<HamsterResponse> getRspClass() {
		return HamsterResponse.class;
	}

	@Override
	public ResponseConvertor<HamsterRequest, HamsterResponse> getResponseAttrWriter() {
		return hamsterResponseAttrWriter;
	}

	@Override
	public RequestConvertor<HamsterRequest> getRequestConvertor() {
		return hamsterRequestAttrWriter;
	}

	@Override
	public ResponseInfoExtractor<HamsterResponse> getResponsetInfoExtractor() {
		return repextractor;
	}

	@Override
	public Class<HamsterRequest> getReqClass() {
		return HamsterRequest.class;
	}

	@Override
	public HamsterClient createClient() {
		return new HamsterClient();
	}

	@Override
	public String protocolName() {
		return "hamster";
	}

	@Override
	public HamsterRequest createRequest() {
		return new HamsterRequest();
	}

	@Override
	public HamsterResponse createResponse() {
		return new HamsterResponse();
	}

}
