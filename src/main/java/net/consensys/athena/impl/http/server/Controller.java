package net.consensys.athena.impl.http.server;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

public interface Controller {
  Responder handle(HttpRequest request, FullHttpResponse response);
}
