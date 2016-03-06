package com.seaky.hamster.core.rpc.protocol.hamster;

import java.util.Map;
import java.util.Map.Entry;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;
import com.seaky.hamster.core.rpc.protocol.ResponseInfo;
import com.seaky.hamster.core.rpc.protocol.hamster.HamsterResponse.ObjectHolder;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.serialization.SerializerFactory;

public class HamsterResponseAttrWriter implements
		ResponseConvertor<HamsterRequest, HamsterResponse> {

	public void writeAttachment(HamsterResponse rsp, String key, String value) {
		if (value == null)
			return;
		rsp.addAttachment(key, value);

	}

	@Override
	public void write(HamsterRequest req, HamsterResponse rsp, ResponseInfo info) {
		Serializer ser = SerializerFactory.getSerializer(Constants.KRYO_SERIAL);
		ObjectHolder holder = new ObjectHolder();
		holder.setObj(info.getResult());
		byte[] resultBytes = ser.serialize(holder);
		rsp.setResult(resultBytes);
		Map<String, String> attachments = info.getAttachments();
		if (attachments != null) {
			for (Entry<String, String> entry : attachments.entrySet()) {
				writeAttachment(rsp, entry.getKey(), entry.getValue());
			}
		}

	}

}
