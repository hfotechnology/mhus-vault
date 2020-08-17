/**
 * Copyright (C) 2020 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.client.example;

import java.io.IOException;

import de.mhus.cherry.vault.client.RsaCipher;
import de.mhus.cherry.vault.client.VaultClientConnection;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemKey;
import de.mhus.lib.errors.MException;

public class ImportSecret {

    static PemKey key =
            new PemKey(
                    PemBlock.BLOCK_PUB,
                    "MIIBtzCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC"
                            + "7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbL"
                            + "m1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1"
                            + "fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3R"
                            + "SAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd"
                            + "7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCB"
                            + "gLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbq"
                            + "N/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1Ul"
                            + "ZAFMO/7PSSoDgYQAAoGAZQ/du8+gqTGtYVyEf5R3gV0Ge21rsI"
                            + "UofAL+TkG+ntDh35wI1y1wCKEE7BcWfVEXcpco9fQK9f1AKbUZ"
                            + "55446yPykUFtrXhJLkCuPYyACVgbEJzcjmec7/EmJZRQDgggbv"
                            + "IH7Pd7it7w084lPz9DTUl0nXF4084nqvhOmaKDfmw=",
                    false);

    public static void main(String[] args) throws IOException, MException {
        VaultClientConnection con =
                new VaultClientConnection("http://localhost:8181", "admin", "123");
        // first encode secret
        String secret = "don't show this";
        PemBlock encoded = RsaCipher.encode(key, secret);
        String secretId = con.importSecret("test", encoded.toString(), null);
        System.out.println("SecretId: " + secretId);
    }
}
