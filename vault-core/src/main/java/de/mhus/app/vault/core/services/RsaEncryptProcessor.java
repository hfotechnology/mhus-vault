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
package de.mhus.app.vault.core.services;

import java.io.PrintStream;
import java.util.UUID;

import org.osgi.service.component.annotations.Component;

import de.mhus.app.vault.api.ifc.SecretContent;
import de.mhus.app.vault.api.ifc.TargetProcessor;
import de.mhus.app.vault.api.model.WritableEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockList;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.keychain.MKeychain;
import de.mhus.lib.core.keychain.MKeychainUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.CryptApi;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;

@Component(property = "name=cipher.rsa")
public class RsaEncryptProcessor implements TargetProcessor {

    @Override
    public void process(
            IProperties properties,
            IReadProperties processorConfig,
            SecretContent secret,
            WritableEntry entry)
            throws MException {

        UUID keyId = UUID.fromString(processorConfig.getString("keyId"));

        CryptApi api = M.l(CryptApi.class);
        CipherProvider cipher =
                api.getCipher(
                        processorConfig.getString("cipherService", CFG_CIPHER_DEFAULT.value()));

        MKeychain vault = MKeychainUtil.loadDefault();
        de.mhus.lib.core.keychain.KeyEntry keyValue = vault.getEntry(keyId);
        if (keyValue == null) throw new NotFoundException("key not found", keyId);

        PemPub key = PemUtil.toKey(keyValue.getValue().value());
        PemBlock encoded = cipher.encrypt(key, secret.getContent().value());

        PemBlockList result = new PemBlockList();
        result.add(encoded);

        SignerUtil.sign(result, processorConfig, encoded.toString());

        entry.setSecret(result.toString());
    }

    @Override
    public void test(PrintStream out, IProperties properties, IReadProperties processorConfig)
            throws Exception {

        UUID keyId = UUID.fromString(processorConfig.getString("keyId"));
        out.println("Key: " + keyId);
        MKeychain vault = MKeychainUtil.loadDefault();
        de.mhus.lib.core.keychain.KeyEntry keyValue = vault.getEntry(keyId);
        if (keyValue == null) throw new NotFoundException("key not found", keyId);
        out.println("Key value: " + keyValue);

        CryptApi api = M.l(CryptApi.class);
        CipherProvider cipher =
                api.getCipher(
                        processorConfig.getString("cipherService", CFG_CIPHER_DEFAULT.value()));
        out.println("Cipher: " + cipher);

        SignerUtil.test(out, properties, processorConfig);
    }
}
