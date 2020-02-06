/*
 * Copyright 2020 ConsenSys AG.
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
package net.consensys.orion.enclave.sodium;

import static org.assertj.core.api.Assertions.assertThat;

import net.consensys.orion.enclave.PrivacyGroupPayload.Type;

import java.security.Security;
import java.util.Arrays;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.sodium.Box.PublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SodiumEnclavePrivacyGroupIdTest {
  private static final String PRIVACY_GROUP_ID1 = "negmDcN2P4ODpqn/6WkJ02zT/0w0bjhGpkZ8UP6vARk=";
  private static final String PRIVACY_GROUP_ID2 = "g59BmTeJIn7HIcnq8VQWgyh/pDbvbt2eyP0Ii60aDDw=";
  private static final String PRIVACY_GROUP_ID3 = "6fg8q5rWMBoAT2oIiU3tYJbk4b7oAr7dxaaVY7TeM3U=";

  private final MemoryKeyStore keyStore = new MemoryKeyStore();
  private SodiumEnclave enclave;

  static {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  }

  @BeforeEach
  void setUp() {
    enclave = new SodiumEnclave(keyStore);
  }

  @Test
  public void generatesSameLegacyPrivacyGroupIdForDuplicateValues() {
    final String expectedPrivacyGroupId = "/xzRjCLioUBkm5LYuzll61GXyrD5x7bvXzQk/ovJA/4=";
    assertThat(legacyPrivacyGroupId(PRIVACY_GROUP_ID2, PRIVACY_GROUP_ID1, PRIVACY_GROUP_ID3, PRIVACY_GROUP_ID1))
        .isEqualTo(expectedPrivacyGroupId);
    assertThat(legacyPrivacyGroupId(PRIVACY_GROUP_ID2, PRIVACY_GROUP_ID1, PRIVACY_GROUP_ID1, PRIVACY_GROUP_ID3))
        .isEqualTo(expectedPrivacyGroupId);
    assertThat(legacyPrivacyGroupId(PRIVACY_GROUP_ID2, PRIVACY_GROUP_ID1, PRIVACY_GROUP_ID3))
        .isEqualTo(expectedPrivacyGroupId);
  }

  @Test
  public void generatesSameLegacyPrivacyGroupIdForPrivateForInDifferentOrders() {
    final String expectedPrivacyGroupId = "/xzRjCLioUBkm5LYuzll61GXyrD5x7bvXzQk/ovJA/4=";
    assertThat(legacyPrivacyGroupId(PRIVACY_GROUP_ID1, PRIVACY_GROUP_ID2, PRIVACY_GROUP_ID3))
        .isEqualTo(expectedPrivacyGroupId);
    assertThat(legacyPrivacyGroupId(PRIVACY_GROUP_ID1, PRIVACY_GROUP_ID3, PRIVACY_GROUP_ID2))
        .isEqualTo(expectedPrivacyGroupId);
    assertThat(legacyPrivacyGroupId(PRIVACY_GROUP_ID2, PRIVACY_GROUP_ID1, PRIVACY_GROUP_ID3))
        .isEqualTo(expectedPrivacyGroupId);
    assertThat(legacyPrivacyGroupId(PRIVACY_GROUP_ID2, PRIVACY_GROUP_ID3, PRIVACY_GROUP_ID1))
        .isEqualTo(expectedPrivacyGroupId);
    assertThat(legacyPrivacyGroupId(PRIVACY_GROUP_ID3, PRIVACY_GROUP_ID1, PRIVACY_GROUP_ID2))
        .isEqualTo(expectedPrivacyGroupId);
    assertThat(legacyPrivacyGroupId(PRIVACY_GROUP_ID3, PRIVACY_GROUP_ID2, PRIVACY_GROUP_ID1))
        .isEqualTo(expectedPrivacyGroupId);
  }

  private String legacyPrivacyGroupId(final String... recipientsAndSender) {
    final PublicKey[] publicKeys =
        Arrays.stream(recipientsAndSender).map(Bytes::fromBase64String).map(PublicKey::fromBytes).toArray(
            PublicKey[]::new);
    return Bytes.of(enclave.generatePrivacyGroupId(publicKeys, null, Type.LEGACY)).toBase64String();
  }
}
