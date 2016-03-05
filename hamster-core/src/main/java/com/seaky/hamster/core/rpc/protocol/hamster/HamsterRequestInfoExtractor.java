package com.seaky.hamster.core.rpc.protocol.hamster;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.protocol.RequestInfo;
import com.seaky.hamster.core.rpc.protocol.RequestInfoExtractor;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.serialization.SerializerFactory;

public class HamsterRequestInfoExtractor implements RequestInfoExtractor<HamsterRequest> {

  @Override
  public RequestInfo extract(HamsterRequest req) {
    String serviceName = req.getServiceName();
    Serializer ser = SerializerFactory.getSerializer(Constants.KRYO_SERIAL);
    Object[] params = ser.deSerialize(req.getParams(), Object[].class);
    RequestInfo info =
        new RequestInfo(req.getApp(), req.getReferApp(), serviceName, req.getServiceVersion(),
            req.getReferVersion(), req.getGroup(), req.getReferGroup(), params);
    info.addAttachment(req.getAttachments());
    return info;

  }

}
