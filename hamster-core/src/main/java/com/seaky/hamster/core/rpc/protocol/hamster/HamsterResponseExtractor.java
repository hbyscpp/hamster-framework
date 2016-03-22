package com.seaky.hamster.core.rpc.protocol.hamster;

import java.util.Map;
import java.util.Map.Entry;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.exception.ClientDeserResultException;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.Response;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;
import com.seaky.hamster.core.rpc.protocol.hamster.HamsterResponse.ObjectHolder;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.serialization.SerializerFactory;

public class HamsterResponseExtractor implements
		ResponseConvertor<HamsterResponse> {

	@Override
	public Response convertFrom(HamsterResponse rsp) {
		Response info = new Response();

		Map<String, String> attachments = rsp.getAttachments();
		Attachments attach = new Attachments();
		if (attachments != null) {
			for (Entry<String, String> entry : attachments.entrySet()) {
				attach.addAttachment(entry.getKey(), entry.getValue());
			}
		}
		info.setAttachments(attach);
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

	@Override
	public HamsterResponse convertTo(Response rsp) {
		HamsterResponse info = new HamsterResponse();
		Serializer ser = SerializerFactory.getSerializer(Constants.KRYO_SERIAL);
		ObjectHolder holder = new ObjectHolder();
		holder.setObj(rsp.getResult());
		byte[] resultBytes = ser.serialize(holder);
		info.setResult(resultBytes);
		Map<String, String> attachments = rsp.getAttachments()
				.getAllAttachments();
		if (attachments != null) {
			for (Entry<String, String> entry : attachments.entrySet()) {
				info.addAttachment(entry.getKey(), entry.getValue());
			}
		}

		return info;

	}

	
}
