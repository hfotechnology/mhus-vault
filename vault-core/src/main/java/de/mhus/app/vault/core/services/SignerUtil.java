/**
 * Copyright (C) 2018 Mike Hummel (mh@mhus.de)
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
package de.mhus.app.vault.core.services;

import java.io.PrintStream;
import java.util.UUID;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockList;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.CryptApi;
import de.mhus.osgi.crypt.api.signer.SignerProvider;

public class SignerUtil {

    public static void test(
            PrintStream out, IProperties properties, IReadProperties processorConfig)
            throws Exception {
        if (processorConfig.isProperty("signId")) {
            UUID signId = UUID.fromString(processorConfig.getString("signId"));
            out.println("Signature: " + signId);
            MKeychain vault = MKeychainUtil.loadDefault();
            de.mhus.lib.core.keychain.KeyEntry signKeyValue = vault.getEntry(signId);
            out.println("Key: " + signKeyValue);
            CryptApi api = M.l(CryptApi.class);
            SignerProvider signer =
                    api.getSigner(processorConfig.getString("signService", "DSA-1"));
            out.println("Signer: " + signer);
        }
    }

    public static void sign(PemBlockList result, IReadProperties processorConfig, String cs)
            throws MException {
        if (processorConfig.isProperty("signId")) {
            MKeychain vault = MKeychainUtil.loadDefault();
            CryptApi api = M.l(CryptApi.class);
            UUID signId = UUID.fromString(processorConfig.getString("signId"));
            SignerProvider signer =
                    api.getSigner(processorConfig.getString("signService", "DSA-1"));
            de.mhus.lib.core.keychain.KeyEntry signKeyValue = vault.getEntry(signId);
            if (signKeyValue == null) throw new NotFoundException("sign key not found", signId);
            PemPriv signKey = PemUtil.toKey(signKeyValue.getValue().value());
            PemBlock signed =
                    signer.sign(signKey, cs, processorConfig.getString("signPassphrase", null));
            result.add(signed);
        }
    }
}
