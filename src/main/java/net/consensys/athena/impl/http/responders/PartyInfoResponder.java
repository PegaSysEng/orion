package net.consensys.athena.impl.http.responders;

import net.consensys.athena.impl.http.server.AbstractResponder;
import net.consensys.athena.impl.http.server.ContentType;
import net.consensys.athena.impl.http.server.Responder;

import io.netty.handler.codec.http.FullHttpResponse;

public class PartyInfoResponder extends AbstractResponder implements Responder {

  public PartyInfoResponder(FullHttpResponse response) {
    super(response, ContentType.HASKELL_ENCODED);
  }

  @Override
  public byte[] getRaw() {
    return new byte[0];
  }

  @Override
  public String getJson() {
    return null;
  }

  @Override
  public byte[] getHaskellEncoded() {
    return new byte[0];
  }
}
