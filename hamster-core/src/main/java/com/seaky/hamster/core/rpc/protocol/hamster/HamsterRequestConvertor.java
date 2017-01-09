package com.seaky.hamster.core.rpc.protocol.hamster;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.exception.ServerDeserParamException;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.protocol.RequestConvertor;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

public class HamsterRequestConvertor implements RequestConvertor<HamsterRequest> {


  @Override
  public ProtocolRequestHeader parseProtocolHeader(HamsterRequest req) {
    ProtocolRequestHeader header = new ProtocolRequestHeader();
    header.setApp(req.getApp());
    header.setGroup(req.getGroup());
    header.setVersion(req.getServiceVersion());
    header.setReferenceApp(req.getReferApp());
    header.setReferenceGroup(req.getReferGroup());
    header.setReferenceVersion(req.getReferVersion());
    Attachments attachments = new Attachments();

    Map<String, Object> attach = req.getAttachments();
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
    header.setServiceName(req.getServiceName());
    return header;
  }

  @Override
  public ProtocolRequestBody parseProtocolBody(HamsterRequest req, ProtocolRequestHeader header,
      Class<?>[] paramTypes) {
    ProtocolRequestBody body = new ProtocolRequestBody();
    try {

      byte[][] pbytes = req.getParams();
      if (pbytes != null && pbytes.length > 0) {
        Serializer ser =
            ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.KRYO_SERIAL);
        int len = pbytes.length;
        Object[] params = new Object[len];
        for (int i = 0; i < len; ++i) {
          params[i] = ser.deSerialize(pbytes[i], paramTypes[i]);
        }
        body.setParams(params);
      }
    } catch (Exception e) {
      throw new ServerDeserParamException(ExceptionUtils.getStackTrace(e));
    }
    return body;
  }

  @Override
  public HamsterRequest createRequest(ProtocolRequestHeader header, ProtocolRequestBody body) {
    Short version = header.getAttachments().getAsShort(Constants.PROTOCOL_VERSION);
    if (version == null || version != 0)
      throw new RuntimeException("not support version " + version);
    HamsterRequest request = new HamsterRequest();
    request.setApp(header.getApp());
    request.setGroup(header.getGroup());
    request.setAttachments(header.getAttachments().getAllKeyValue());
    if (body != null) {
      Object[] params = body.getParams();
      if (params != null && params.length > 0) {
        Serializer ser =
            ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.KRYO_SERIAL);
        int len = params.length;
        byte[][] pbytes = new byte[len][];
        for (int i = 0; i < len; ++i) {
          pbytes[i] = ser.serialize(params[i]);
        }
        request.setParams(pbytes);
      }
    }
    request.setReferApp(header.getReferenceApp());
    request.setReferGroup(header.getReferenceGroup());
    request.setReferVersion(header.getReferenceVersion());
    request.setServiceName(header.getServiceName());
    request.setServiceVersion(header.getVersion());
    return request;
  }

}
