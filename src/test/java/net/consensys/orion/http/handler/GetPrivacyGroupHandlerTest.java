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
package net.consensys.orion.http.handler;

import static net.consensys.cava.io.Base64.encodeBytes;
import static net.consensys.orion.http.server.HttpContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import net.consensys.cava.crypto.sodium.Box;
import net.consensys.orion.enclave.Enclave;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import net.consensys.orion.enclave.sodium.MemoryKeyStore;
import net.consensys.orion.enclave.sodium.SodiumEnclave;
import net.consensys.orion.helpers.FakePeer;
import net.consensys.orion.http.handler.privacy.DeletePrivacyGroupRequest;
import net.consensys.orion.http.handler.privacy.GetPrivacyGroupRequest;
import net.consensys.orion.http.handler.privacy.PrivacyGroup;
import net.consensys.orion.http.handler.privacy.PrivacyGroupRequest;
import net.consensys.orion.utils.Serializer;

import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Collections;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetPrivacyGroupHandlerTest extends HandlerTest {
  private MemoryKeyStore memoryKeyStore;
  private String privacyGroupId;
  private FakePeer fakePeer;
  private Box.PublicKey senderKey;
  private String[] members;

  @Override
  protected Enclave buildEnclave(final Path tempDir) {
    memoryKeyStore = new MemoryKeyStore();
    final Box.PublicKey defaultNodeKey = memoryKeyStore.generateKeyPair();
    memoryKeyStore.addNodeKey(defaultNodeKey);
    return new SodiumEnclave(memoryKeyStore);
  }

  @BeforeEach
  void setup() throws IOException, InterruptedException {
    senderKey = memoryKeyStore.generateKeyPair();
    final Box.PublicKey recipientKey = memoryKeyStore.generateKeyPair();

    members = new String[] {encodeBytes(senderKey.bytesArray()), encodeBytes(recipientKey.bytesArray())};
    final PrivacyGroupRequest privacyGroupRequestExpected =
        buildPrivacyGroupRequest(members, encodeBytes(senderKey.bytesArray()), "test", "desc");
    final Request request = buildPrivateAPIRequest("/createPrivacyGroup", JSON, privacyGroupRequestExpected);

    final byte[] privacyGroupPayload = enclave.generatePrivacyGroupId(
        new Box.PublicKey[] {senderKey, recipientKey},
        privacyGroupRequestExpected.getSeed().get(),
        PrivacyGroupPayload.Type.PANTHEON);

    // create fake peer
    fakePeer = new FakePeer(new MockResponse().setBody(encodeBytes(privacyGroupPayload)), recipientKey);
    networkNodes.addNode(Collections.singletonList(fakePeer.publicKey), fakePeer.getURL());

    // execute request
    final Response resp = httpClient.newCall(request).execute();

    assertEquals(200, resp.code());

    final RecordedRequest recordedRequest = fakePeer.server.takeRequest();
    assertEquals("/pushPrivacyGroup", recordedRequest.getPath());
    assertEquals("POST", recordedRequest.getMethod());

    final PrivacyGroup privacyGroup = Serializer.deserialize(JSON, PrivacyGroup.class, resp.body().bytes());
    privacyGroupId = privacyGroup.getPrivacyGroupId();
  }

  @Test
  void retrievesPrivacyGroupDetails() throws Exception {
    final GetPrivacyGroupRequest getPrivacyGroupRequest = buildGetPrivacyGroupRequest(privacyGroupId);
    final Request request = buildPrivateAPIRequest("/getPrivacyGroup", JSON, getPrivacyGroupRequest);
    fakePeer.addResponse(new MockResponse().setBody(privacyGroupId));

    final Response resp = httpClient.newCall(request).execute();

    assertThat(resp.code()).isEqualTo(200);
    final PrivacyGroup privacyGroup = Serializer.deserialize(JSON, PrivacyGroup.class, resp.body().bytes());
    assertThat(privacyGroup.getPrivacyGroupId()).isEqualTo(privacyGroupId);
    assertThat(privacyGroup.getName()).isEqualTo("test");
    assertThat(privacyGroup.getDescription()).isEqualTo("desc");
    assertThat(privacyGroup.getMembers()).isEqualTo(members);
  }

  @Test
  void unknownPrivacyGroupIdFails() throws IOException {
    final GetPrivacyGroupRequest getPrivacyGroupRequest = buildGetPrivacyGroupRequest("unknownPrivacyGroupId");
    final Request request = buildPrivateAPIRequest("/getPrivacyGroup", JSON, getPrivacyGroupRequest);

    final Response resp = httpClient.newCall(request).execute();

    assertThat(resp.code()).isEqualTo(404);
  }

  @Test
  void deletedPrivacyGroupFails() throws IOException {
    fakePeer.addResponse(new MockResponse().setBody(privacyGroupId));

    final DeletePrivacyGroupRequest deletePrivacyGroupRequest =
        buildDeletePrivacyGroupRequest(privacyGroupId, encodeBytes(senderKey.bytesArray()));
    final Request deleteRequest = buildPrivateAPIRequest("/deletePrivacyGroup", JSON, deletePrivacyGroupRequest);
    final Response deleteResponse = httpClient.newCall(deleteRequest).execute();
    assertThat(deleteResponse.code()).isEqualTo(200);

    final GetPrivacyGroupRequest getPrivacyGroupRequest = buildGetPrivacyGroupRequest(privacyGroupId);
    final Request getGroupRequest = buildPrivateAPIRequest("/getPrivacyGroup", JSON, getPrivacyGroupRequest);
    final Response getGroupResponse = httpClient.newCall(getGroupRequest).execute();
    assertThat(getGroupResponse.code()).isEqualTo(404);
  }

  @Test
  void requestWithoutPrivacyGroupIdFails() throws IOException {
    final GetPrivacyGroupRequest getPrivacyGroupRequest = buildGetPrivacyGroupRequest(null);
    final Request request = buildPrivateAPIRequest("/getPrivacyGroup", JSON, getPrivacyGroupRequest);

    final Response resp = httpClient.newCall(request).execute();

    assertThat(resp.code()).isEqualTo(400);
  }

  PrivacyGroupRequest buildPrivacyGroupRequest(
      final String[] addresses,
      final String from,
      final String name,
      final String description) {
    final PrivacyGroupRequest privacyGroupRequest = new PrivacyGroupRequest(addresses, from, name, description);
    // create a random seed
    final SecureRandom random = new SecureRandom();
    final byte[] bytes = new byte[20];
    random.nextBytes(bytes);
    privacyGroupRequest.setSeed(bytes);

    return privacyGroupRequest;
  }

  GetPrivacyGroupRequest buildGetPrivacyGroupRequest(final String key) {
    return new GetPrivacyGroupRequest(key);
  }

  DeletePrivacyGroupRequest buildDeletePrivacyGroupRequest(final String key, final String from) {
    return new DeletePrivacyGroupRequest(key, from);
  }
}