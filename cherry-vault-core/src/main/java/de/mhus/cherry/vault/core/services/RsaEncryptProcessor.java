package de.mhus.cherry.vault.core.services;

import java.util.UUID;

import aQute.bnd.annotation.component.Component;
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
import de.mhus.lib.core.vault.MVault;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.CryptaApi;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;
import de.mhus.osgi.crypt.api.signer.SignerProvider;

@Component(properties="name=cipher.rsa")
public class RsaEncryptProcessor implements TargetProcessor {

	@Override
	public void process(IProperties properties, IReadProperties processorConfig, SecretContent secret,
			WritableEntry entry) throws MException {

		UUID keyId = UUID.fromString(processorConfig.getString("keyId"));
		
		CryptaApi api = MApi.lookup(CryptaApi.class);
		CipherProvider cipher = api.getCipher(processorConfig.getString("cipherService","RSA-1"));
		
		MVault vault = MVaultUtil.loadDefault();
		de.mhus.lib.core.vault.VaultEntry keyValue = vault.getEntry(keyId);
		if (keyValue == null) throw new NotFoundException("key not found",keyId);
		
		PemPub key = keyValue.adaptTo(PemPub.class);
		PemBlock encoded = cipher.encode(key, secret.getContent().value());
		
		PemBlockList result = new PemBlockList();
		result.add(encoded);
		
		if (processorConfig.isProperty("signId")) {
			UUID signId = UUID.fromString(processorConfig.getString("signId"));
			SignerProvider signer = api.getSigner(processorConfig.getString("signService", "DSA-1"));
			de.mhus.lib.core.vault.VaultEntry signKeyValue = vault.getEntry(signId);
			if (signKeyValue == null) throw new NotFoundException("sign key not found",signId);
			PemPriv signKey = signKeyValue.adaptTo(PemPriv.class);
			PemBlock signed = signer.sign(signKey, encoded.toString(), processorConfig.getString("signPassphrase", null));
			result.addFirst(signed);
		}
		
		entry.setSecretKeyId(keyId.toString());
		entry.setSecret(result.toString());
		
	}

}
