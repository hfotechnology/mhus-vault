/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.core.services;

import java.util.UUID;

import org.osgi.service.component.annotations.Component;
import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.ifc.TargetProcessor;
import de.mhus.cherry.vault.api.model.WritableEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockList;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.core.crypt.pem.PemUtil;
import de.mhus.lib.core.vault.MVault;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.CryptApi;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;
import de.mhus.osgi.crypt.api.signer.SignerProvider;

@Component(property="name=cipher.rsa")
public class RsaEncryptProcessor implements TargetProcessor {

	@Override
	public void process(IProperties properties, IReadProperties processorConfig, SecretContent secret,
			WritableEntry entry) throws MException {

		UUID keyId = UUID.fromString(processorConfig.getString("keyId"));
		
		CryptApi api = MApi.lookup(CryptApi.class);
		CipherProvider cipher = api.getCipher(processorConfig.getString("cipherService","RSA-1"));
		
		MVault vault = MVaultUtil.loadDefault();
		de.mhus.lib.core.vault.VaultEntry keyValue = vault.getEntry(keyId);
		if (keyValue == null) throw new NotFoundException("key not found",keyId);
		
		PemPub key = PemUtil.toKey(keyValue.getValue().value());
		PemBlock encoded = cipher.encrypt(key, secret.getContent().value());
		
		PemBlockList result = new PemBlockList();
		result.add(encoded);
		
		if (processorConfig.isProperty("signId")) {
			UUID signId = UUID.fromString(processorConfig.getString("signId"));
			SignerProvider signer = api.getSigner(processorConfig.getString("signService", "DSA-1"));
			de.mhus.lib.core.vault.VaultEntry signKeyValue = vault.getEntry(signId);
			if (signKeyValue == null) throw new NotFoundException("sign key not found",signId);
			PemPriv signKey = PemUtil.toKey(signKeyValue.getValue().value());
			PemBlock signed = signer.sign(signKey, encoded.toString(), processorConfig.getString("signPassphrase", null));
			result.addFirst(signed);
		}
		
		entry.setSecretKeyId(keyId.toString());
		entry.setSecret(result.toString());
		
	}

}
