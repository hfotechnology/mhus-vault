package de.mhus.cherry.vault.client;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;

public class EccSigner {

	public static boolean validate(PemPub key, String text, PemBlock sign) throws MException {
		init();
		try {
			byte[] encKey = key.getBytesBlock();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
			
			Signature sig = Signature.getInstance("SHA512WITHECDSA", "BC");
			sig.initVerify(pubKey);
			
			byte[] buffer = text.getBytes();
			sig.update(buffer, 0, buffer.length);

			byte[] sigToVerify = sign.getBytesBlock();
			return sig.verify(sigToVerify);
			
		} catch (Exception e) {
			throw new MException(e);
		}
	}

	public static void init() {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
			Security.addProvider(new BouncyCastleProvider());
	}

}
