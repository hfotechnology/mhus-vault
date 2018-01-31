package de.mhus.cherry.vault.core.services;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.ifc.TargetProcessor;
import de.mhus.cherry.vault.api.model.WritableEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.crypt.MRandom;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockList;
import de.mhus.lib.core.crypt.pem.PemBlockModel;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.vault.MVault;
import de.mhus.lib.core.vault.MVaultUtil;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.crypt.api.CryptaApi;
import de.mhus.osgi.crypt.api.signer.SignerProvider;

@Component(properties="name=hash.md5")
public class Md5Processor implements TargetProcessor {

	@Override
	public void process(IProperties properties, IReadProperties processorConfig, SecretContent secret,
	        WritableEntry entry) throws MException {
		
		try {
			
			MRandom random = MApi.lookup(MRandom.class);
			String salt = "" + random.getChar() + random.getChar();
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(salt.getBytes("utf-8"));
			md.update(secret.getContent().value().getBytes("utf-8"));
			byte[] digest = md.digest();
			String cs = Base64.getEncoder().encodeToString(digest);
			
			PemBlockList result = new PemBlockList();
			PemBlockModel block = new PemBlockModel(PemBlock.BLOCK_HASH, cs);
			block.setString(PemBlock.METHOD, "MD5");
			block.setString("Salt", salt);
			block.setString(PemBlock.STRING_ENCODING, "utf-8");
			result.add(block);
			
			if (processorConfig.isProperty("signId")) {
				MVault vault = MVaultUtil.loadDefault();
				CryptaApi api = MApi.lookup(CryptaApi.class);
				UUID signId = UUID.fromString(processorConfig.getString("signId"));
				SignerProvider signer = api.getSigner(processorConfig.getString("signService", "DSA-1"));
				de.mhus.lib.core.vault.VaultEntry signKeyValue = vault.getEntry(signId);
				if (signKeyValue == null) throw new NotFoundException("sign key not found",signId);
				PemPriv signKey = signKeyValue.adaptTo(PemPriv.class);
				PemBlock signed = signer.sign(signKey, cs, processorConfig.getString("signPassphrase", null));
				result.addFirst(signed);
			}

			entry.setSecret(result.toString());

		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new MException(e);
		}
		
	}

}
