package com.seaky.hamster.core.rpc.protocol.http;

import java.util.HashMap;
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
import com.seaky.hamster.core.rpc.utils.ExtensionLoaderConstants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

public class HttpResponseConvertor implements ResponseConvertor<FullHttpResponse> {

  @Override
  public ProtocolResponseHeader parseHeader(FullHttpResponse rsp) {
    ProtocolResponseHeader header = new ProtocolResponseHeader();
    HttpHeaders headers = rsp.headers();
    String attchStr = headers.get(HttpConstans.ATTACHMENTS_HEADER_KEY);
    // 反序列化

    Map<String, Object> attach = new HashMap<>();
    Attachments attachments = new Attachments();
    if (attchStr != null) {
      Serializer jsonser =
          ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.JSON_SERIAL);
      attach = jsonser.deSerialize(attchStr.getBytes(Constants.UTF_8), Map.class);
      for (Entry<String, Object> entry : attach.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        attachments.putValue(key, value);
      }
    }
    header.setAttachments(attachments);
    header.setException(Boolean.valueOf(headers.get(HttpConstans.EXCEPTION_HEADER_KEY)));
    return header;
  }

  @Override
  public ProtocolResponseBody parseBody(FullHttpResponse rsp, ProtocolResponseHeader header,
      Class<?> type) {
    ProtocolResponseBody info = new ProtocolResponseBody();


    if (HttpUtil.getContentLength(rsp, 0) > 0) {
      Serializer ser =
          ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.MSGPACK_SERIAL);
      Object result = null;
      byte[] body = new byte[rsp.content().readableBytes()];
      rsp.content().readBytes(body);
      try {
        if (header.isException()) {
          result = ser.deSerialize(body, ExceptionResult.class);
        } else {
          result = ser.deSerialize(body, type);
        }
        info.setResult(result);
      } catch (Exception e) {
        info.setResult(new ClientDeserResultException(e));
        header.setException(true);
      }
    }

    return info;

  }

  @Override
  public FullHttpResponse createResponse(ProtocolResponseHeader header, ProtocolResponseBody body) {


    Serializer jsonser =
        ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.JSON_SERIAL);
    Map<String, Object> attachments = header.getAttachments().getAllKeyValue();
    HttpHeaders headers = new DefaultHttpHeaders();
    if (attachments != null && attachments.size() > 0) {
      byte[] attch = jsonser.serialize(attachments);
      headers.add(HttpConstans.ATTACHMENTS_HEADER_KEY, new String(attch, Constants.UTF_8));
    }
    headers.add(HttpConstans.EXCEPTION_HEADER_KEY, header.isException());
    Serializer ser =
        ExtensionLoaderConstants.SERIALIZER_EXTENSION.findExtension(Constants.MSGPACK_SERIAL);
    ByteBuf result = Unpooled.EMPTY_BUFFER;
    if (body != null && body.getResult() != null) {
      byte[] resultBytes = ser.serialize(body.getResult());
      result = Unpooled.wrappedBuffer(resultBytes);
    }
    FullHttpResponse httprsp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
        HttpResponseStatus.OK, result, headers, new DefaultHttpHeaders());
    httprsp.headers().add(HttpHeaderNames.CONTENT_LENGTH, result.readableBytes());
    return httprsp;


  }



}
