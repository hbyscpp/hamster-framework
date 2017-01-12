package com.seaky.hamster.core.rpc.protocol.hamster;

import java.util.Map;
import java.util.Map.Entry;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.exception.ClientDeserResultException;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.ExceptionResult;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolResponseHeader;
import com.seaky.hamster.core.rpc.protocol.ResponseConvertor;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.serialization.SerializerManager;

public class HamsterResponseConvertor implements ResponseConvertor<HamsterResponse> {


  @Override
  public ProtocolResponseHeader parseHeader(HamsterResponse rsp) {
    ProtocolResponseHeader header = new ProtocolResponseHeader();
    Map<String, Object> attach = rsp.getAttachments();

    Attachments attachments = new Attachments();

    for (Entry<String, Object> entry : attach.entrySet()) {

      String key = entry.getKey();
      Object value = entry.getValue();

      if (value instanceof Byte) {
        attachments.putByte(key, (Byte) value);
      } else if (value instanceof Short) {
        attachments.putShort(key, (Short) value);
      } else if (value instanceof Integer) {
        attachments.putInt(key, (Integer) value);
      } else if (value instanceof Long) {
        attachments.putLong(key, (Long) value);
      } else if (value instanceof Float) {
        attachments.putFloat(key, (Float) value);
      } else if (value instanceof Double) {
        attachments.putDouble(key, (Double) value);
      } else if (value instanceof String) {
        attachments.putString(key, (String) value);
      } else if (value instanceof Boolean) {
        attachments.putBoolean(key, (Boolean) value);
      }

    }

    header.setAttachments(attachments);
    header.setException(rsp.isException());
    return header;
  }

  @Override
  public ProtocolResponseBody parseBody(HamsterResponse rsp, ProtocolResponseHeader header,
      Class<?> type) {
    ProtocolResponseBody info = new ProtocolResponseBody();

    Serializer ser = SerializerManager
        .getById(header.getAttachments().getAsByte(Constants.SERIALIZATION_ID_KEY));
    Object result = null;
    try {
      if (rsp.isException()) {
        result = ser.deSerialize(rsp.getResult(), ExceptionResult.class);
      } else {
        result = ser.deSerialize(rsp.getResult(), type);
      }
      info.setResult(result);
    } catch (Exception e) {
      info.setResult(new ClientDeserResultException(e));
      header.setException(true);
    }
    return info;

  }



  @Override
  public HamsterResponse createResponse(ProtocolResponseHeader header, ProtocolResponseBody body) {
    HamsterResponse rsp = new HamsterResponse();
    if (header != null) {
      rsp.setAttachments(header.getAttachments().getAllKeyValue());
    }
    rsp.setException(header.isException());
    if (body != null) {
      if (body.getResult() != null) {
        Serializer ser = SerializerManager
            .getById(header.getAttachments().getAsByte(Constants.SERIALIZATION_ID_KEY));
        rsp.setResult(ser.serialize(body.getResult()));
      }
    }
    return rsp;

  }


}
