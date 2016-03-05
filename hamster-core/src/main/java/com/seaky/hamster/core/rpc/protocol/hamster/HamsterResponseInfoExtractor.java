package com.seaky.hamster.core.rpc.protocol.hamster;


import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.exception.ClientDeserResultException;
import com.seaky.hamster.core.rpc.protocol.ResponseInfo;
import com.seaky.hamster.core.rpc.protocol.ResponseInfoExtractor;
import com.seaky.hamster.core.rpc.protocol.hamster.HamsterResponse.ObjectHolder;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.serialization.SerializerFactory;

public class HamsterResponseInfoExtractor implements
		ResponseInfoExtractor<HamsterResponse> {

	@Override
	public ResponseInfo extractor(HamsterResponse rsp) {
		ResponseInfo info = new ResponseInfo();
		info.setAttachments(rsp.getAttachments());
		Serializer ser = SerializerFactory.getSerializer(Constants.KRYO_SERIAL);
		try {
			ObjectHolder holder = ser.deSerialize(rsp.getResult(),
					ObjectHolder.class);
			info.setResult(holder.getObj());
		} catch (Exception e) {
			info.setResult(new ClientDeserResultException(e));
		}
		return info;
	}

}
