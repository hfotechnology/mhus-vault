package de.mhus.cherry.vault.client;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;

public class DsaSigner {

	public static boolean validate(PemPub key, String text, PemBlock sign) throws MException {
		try {
			byte[] encKey = key.getBytesBlock();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
			
			Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
			sig.initVerify(pubKey);
			
			byte[] buffer = text.getBytes();
			sig.update(buffer, 0, buffer.length);

			byte[] sigToVerify = sign.getBytesBlock();
			return sig.verify(sigToVerify);
			
		} catch (Exception e) {
			throw new MException(e);
		}
	}

}
