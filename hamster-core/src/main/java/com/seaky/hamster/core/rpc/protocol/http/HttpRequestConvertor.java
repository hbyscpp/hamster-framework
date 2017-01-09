package com.seaky.hamster.core.rpc.protocol.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.seaky.hamster.core.rpc.common.Constants;
import com.seaky.hamster.core.rpc.exception.ServiceSigMismatchException;
import com.seaky.hamster.core.rpc.protocol.Attachments;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestBody;
import com.seaky.hamster.core.rpc.protocol.ProtocolRequestHeader;
import com.seaky.hamster.core.rpc.protocol.RequestConvertor;
import com.seaky.hamster.core.rpc.serialization.Serializer;
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

public class HttpRequestConvertor implements RequestConvertor<FullHttpRequest> {



  @Override
  public ProtocolRequestHeader parseProtocolHeader(FullHttpRequest req) {
    ProtocolRequestHeader header = new ProtocolRequestHeader();
    HttpHeaders headers = req.headers();
    header.setApp(headers.get(HttpConstans.APP_HEADER_KEY));
    header.setGroup(headers.get(HttpConstans.GROUP_HEADER_KEY));
    header.setVersion(headers.get(HttpConstans.VERSION_HEADER_KEY));
    header.setReferenceApp(headers.get(HttpConstans.REFERENCE_APP_HEADER_KEY));
    header.setReferenceGroup(headers.get(HttpConstans.REFERENCE_GROUP_HEADER_KEY));
    header.setReferenceVersion(headers.get(HttpConstans.REFERENCE_VERSION_HEADER_KEY));
    header.setServiceName(headers.get(HttpConstans.SERVICENAME_HEADER_KEY));
    String attchStr = headers.get(HttpConstans.ATTACHMENTS_HEADER_KEY);
    // 反序列化
    Serializer jsonser =
        ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.JSON_SERIAL);
    Map<String, Object> attach = new HashMap<>();
    if (attchStr != null) {
      attach = jsonser.deSerialize(attchStr.getBytes(Constants.UTF_8), Map.class);
    }
    Attachments attachments = new Attachments();
    if (attach != null) {
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
    }
    header.setAttachments(attachments);
    return header;
  }

  @Override
  public ProtocolRequestBody parseProtocolBody(FullHttpRequest req, ProtocolRequestHeader header,
      Class<?>[] paramTypes) {
    ProtocolRequestBody body = new ProtocolRequestBody();
    int serviceLen = (paramTypes == null ? 0 : paramTypes.length);
    int paramLen = 0;
    if (HttpUtil.getContentLength(req, 0) > 0) {
      Serializer ser =
          ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.MSGPACK_SERIAL);
      ByteBuf buf = req.content();
      byte[] content = new byte[buf.readableBytes()];
      buf.readBytes(content);
      byte[][] pbytes = null;
      String[] pstr = null;
      if (!ser.isText()) {
        pbytes = ser.deSerialize(content, byte[][].class);
        paramLen = pbytes.length;
      } else {
        pstr = ser.deSerialize(content, String[].class);
        paramLen = pstr.length;
      }
      if (paramLen != serviceLen) {
        throw new ServiceSigMismatchException(header.getServiceName());
      }
      Object[] params = new Object[paramLen];
      for (int i = 0; i < paramLen; ++i) {
        if (!ser.isText())
          params[i] = ser.deSerialize(pbytes[i], paramTypes[i]);
        else
          params[i] = ser.deSerialize(pstr[i].getBytes(Constants.UTF_8), paramTypes[i]);
      }
      body.setParams(params);
    } else {
      if (paramLen != serviceLen) {
        throw new ServiceSigMismatchException(header.getServiceName());
      }
    }

    return body;
  }

  private void addHeader(HttpHeaders headers, String key, Object value) {
    if (value == null)
      return;
    headers.set(key, value);
  }

  @Override
  public FullHttpRequest createRequest(ProtocolRequestHeader header, ProtocolRequestBody body) {

    HttpHeaders headers = new DefaultHttpHeaders();
    addHeader(headers, HttpConstans.APP_HEADER_KEY, header.getApp());
    addHeader(headers, HttpConstans.REFERENCE_APP_HEADER_KEY, header.getReferenceApp());

    addHeader(headers, HttpConstans.VERSION_HEADER_KEY, header.getVersion());
    addHeader(headers, HttpConstans.REFERENCE_VERSION_HEADER_KEY, header.getReferenceVersion());

    addHeader(headers, HttpConstans.GROUP_HEADER_KEY, header.getGroup());
    addHeader(headers, HttpConstans.REFERENCE_GROUP_HEADER_KEY, header.getReferenceGroup());
    addHeader(headers, HttpConstans.SERVICENAME_HEADER_KEY, header.getServiceName());

    Serializer jsonser =
        ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.JSON_SERIAL);

    Map<String, Object> attachment = header.getAttachments().getAllKeyValue();
    if (attachment != null) {
      byte[] attach = jsonser.serialize(attachment);
      String attachstr = new String(attach, Constants.UTF_8);
      addHeader(headers, HttpConstans.ATTACHMENTS_HEADER_KEY, attachstr);
    }
    ByteBuf content = Unpooled.EMPTY_BUFFER;
    Object[] params = null;
    if (body != null)
      params = body.getParams();
    if (params != null && params.length > 0) {
      Serializer ser =
          ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.MSGPACK_SERIAL);

      byte[][] pbytes = null;
      String[] pstr = null;
      if (ser.isText()) {
        pstr = new String[params.length];
      } else {
        pbytes = new byte[params.length][];
      }
      int len = params.length;
      for (int i = 0; i < len; ++i) {
        if (!ser.isText()) {
          pbytes[i] = ser.serialize(params[i]);
        } else {
          pstr[i] = new String(ser.serialize(params[i]), Constants.UTF_8);
        }
      }
      if (ser.isText()) {
        content = Unpooled.wrappedBuffer(ser.serialize(pstr));
      } else {
        content = Unpooled.wrappedBuffer(ser.serialize(pbytes));
      }
    }
    FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/",
        content, headers, new DefaultHttpHeaders());
    req.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
    return req;

  }

}
