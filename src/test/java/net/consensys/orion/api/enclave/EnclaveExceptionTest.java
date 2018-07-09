/*
 * Copyright 2018 ConsenSys AG.
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

package net.consensys.orion.api.enclave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.consensys.orion.api.exception.OrionErrorCode;

import org.junit.jupiter.api.Test;

class EnclaveExceptionTest {

  @Test
  void implementationOfRuntimeInterface() {
    assertTrue(RuntimeException.class.isAssignableFrom(EnclaveException.class));
  }

  @Test
  void construction() {
    final String message = "This is the cause";
    final EnclaveException exception = new EnclaveException(OrionErrorCode.ENCLAVE_CREATE_KEY_PAIR, message);
    assertEquals(message, exception.getMessage());
    assertEquals(OrionErrorCode.ENCLAVE_CREATE_KEY_PAIR, exception.code());

    final Throwable cause = new Throwable();
    final EnclaveException anotherException = new EnclaveException(OrionErrorCode.ENCLAVE_STORAGE_ENCRYPT, cause);
    assertEquals(cause, anotherException.getCause());
    assertEquals(OrionErrorCode.ENCLAVE_STORAGE_ENCRYPT, anotherException.code());
  }
}
