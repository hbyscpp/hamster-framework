package com.seaky.hamster.core.rpc.protocol.hamster;

import java.util.Map;
import java.util.Map.Entry;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.common.ServiceContext;
import com.seaky.hamster.core.rpc.common.ServiceContextUtils;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.RequestExtractor;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

public class HamsterRequestExtractor implements RequestExtractor<HamsterRequest> {


  // TODO 抛出异常
  @Override
  public void extractTo(HamsterRequest req, ServiceContext context) {
    ServiceContextUtils.setApp(context, req.getApp());
    ServiceContextUtils.setReferenceApp(context, req.getReferApp());
    ServiceContextUtils.setVersion(context, req.getServiceVersion());
    ServiceContextUtils.setReferenceVersion(context, req.getReferVersion());
    ServiceContextUtils.setGroup(context, req.getGroup());
    ServiceContextUtils.setReferenceGroup(context, req.getReferGroup());
    ServiceContextUtils.setServiceName(context, req.getServiceName());
    Map<String, String> attachments = req.getAttachments();
    Attachments attach = new Attachments();
    if (attachments != null) {
      for (Entry<String, String> entry : attachments.entrySet()) {
        attach.addAttachment(entry.getKey(), entry.getValue());
      }
    }
    ServiceContextUtils.setRequestAttachments(context, attach);
    Serializer ser =
        ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.KRYO_SERIAL);
    Object[] params = ser.deSerialize(req.getParams(), Object[].class);
    ServiceContextUtils.setRequestParams(context, params);
  }

  @Override
  public HamsterRequest extractFrom(ServiceContext context) {
    HamsterRequest req = new HamsterRequest();
    req.setApp(ServiceContextUtils.getApp(context));
    req.setReferApp(ServiceContextUtils.getReferenceApp(context));
    req.setReferVersion(ServiceContextUtils.getReferenceVersion(context));
    req.setServiceVersion(ServiceContextUtils.getVersion(context));
    req.setServiceName(ServiceContextUtils.getServiceName(context));
    req.setGroup(ServiceContextUtils.getGroup(context));
    req.setReferGroup(ServiceContextUtils.getReferenceGroup(context));
    Serializer ser =
        ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.KRYO_SERIAL);
    req.setParams(ser.serialize(ServiceContextUtils.getRequestParams(context)));
    Map<String, String> attachment =
        ServiceContextUtils.getRequestAttachments(context).getAllAttachments();
    if (attachment != null) {
      for (Entry<String, String> entry : attachment.entrySet()) {
        if (entry.getValue() != null) {
          req.addAttachment(entry.getKey(), entry.getValue());
        }
      }
    }

    return req;
  }

}
