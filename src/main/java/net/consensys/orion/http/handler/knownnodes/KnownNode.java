/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.consensys.orion.http.handler.knownnodes;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.postgresql.util.Base64;

class KnownNode {

  private final String publicKey;
  private final String nodeURI;

  KnownNode(final Bytes publicKey, final URI nodeURI) {
    this.publicKey = Base64.encodeBytes(publicKey.toArrayUnsafe());
    this.nodeURI = nodeURI.toString();
  }

  @JsonCreator
  KnownNode(@JsonProperty("publicKey") final String publicKey, final @JsonProperty("nodeUrl") String nodeURI) {
    this.publicKey = publicKey;
    this.nodeURI = nodeURI;
  }

  @JsonProperty("publicKey")
  String getPublicKey() {
    return publicKey;
  }

  @JsonProperty("nodeUrl")
  String getNodeURI() {
    return nodeURI;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final KnownNode knownNode = (KnownNode) o;
    return Objects.equal(publicKey, knownNode.publicKey) && Objects.equal(nodeURI, knownNode.nodeURI);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(publicKey, nodeURI);
  }
}
