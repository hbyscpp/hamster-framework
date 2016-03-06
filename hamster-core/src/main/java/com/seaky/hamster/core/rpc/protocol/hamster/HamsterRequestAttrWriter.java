package com.seaky.hamster.core.rpc.protocol.hamster;

import java.util.Map;
import java.util.Map.Entry;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.protocol.RequestConvertor;
import com.seaky.hamster.core.rpc.protocol.RequestInfo;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.serialization.SerializerFactory;

public class HamsterRequestAttrWriter implements RequestConvertor<HamsterRequest> {

  public void writeAttachment(HamsterRequest req, String key, String value) {
    if (value == null)
      return;
    req.addAttachment(key, value);
  }

  @Override
  public HamsterRequest convert(RequestInfo info) {
	HamsterRequest req=new HamsterRequest();
    req.setApp(info.getApp());
    req.setReferVersion(info.getReferVersion());
    req.setServiceVersion(info.getVersion());
    req.setReferApp(info.getReferApp());
    req.setServiceName(info.getServiceName());
    req.setGroup(info.getGroup());
    req.setReferGroup(info.getReferGroup());
    Serializer ser = SerializerFactory.getSerializer(Constants.KRYO_SERIAL);
    req.setParams(ser.serialize(info.getParams()));
    Map<String, String> attachment = info.getAttachments();
    if (attachment != null) {
      for (Entry<String, String> entry : attachment.entrySet()) {
        writeAttachment(req, entry.getKey(), entry.getValue());
      }
    }
    
    return req;
  }

}
