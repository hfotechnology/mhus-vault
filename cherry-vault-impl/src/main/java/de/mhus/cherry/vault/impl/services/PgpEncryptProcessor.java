package de.mhus.cherry.vault.impl.services;

import java.io.IOException;
import java.util.UUID;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.ifc.TargetProcessor;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.api.model.WritableEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.core.vault.MVault;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.errors.NotSupportedException;
import de.mhus.osgi.crypt.api.CryptaApi;
import de.mhus.osgi.crypt.api.cipher.CipherProvider;

@Component(properties="name=pgp")
public class PgpEncryptProcessor implements TargetProcessor {

	@Override
	public void process(IProperties properties, IReadProperties processorConfig, SecretContent secret,
			WritableEntry entry) throws MException {

		UUID keyId = UUID.fromString(processorConfig.getString("keyId"));
		
		CryptaApi api = MApi.lookup(CryptaApi.class);
		CipherProvider cipher = api.getCipher(CryptaApi.CIPHER_PGP);
		
		MVault vault = MVaultUtil.loadDefault();
		de.mhus.lib.core.vault.VaultEntry keyValue = vault.getEntry(keyId);
		if (keyValue == null) throw new NotFoundException("key not found",keyId);
		
		PemPub key;
		try {
			key = keyValue.adaptTo(PemPub.class);
		} catch (IOException e) {
			throw new MException(e);
		}
		
		PemBlock encoded = cipher.encode(key, secret.getContent().value());
		
		entry.setSecretId(keyId.toString());
		entry.setSecret(encoded.getBlock());
		
	}

}
