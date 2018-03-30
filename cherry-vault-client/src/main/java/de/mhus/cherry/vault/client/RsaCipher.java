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
package de.mhus.cherry.vault.client;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import de.mhus.lib.core.crypt.Blowfish;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.errors.MException;

public class RsaCipher {

	public static String decode(PemPriv key, PemBlock encoded, String passphrase) throws MException {
		try {
			byte[] encKey = key.getBytesBlock();
			if (passphrase != null)
				encKey = Blowfish.decrypt(encKey, passphrase);
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			
			int length = key.getInt(PemBlock.LENGTH, 1024);
			int blockSize = Math.max(length / 1024 * 128, 64);
			
			byte[] b = encoded.getBytesBlock();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			int off = 0;
			while (off < b.length) {
				int len = Math.min(blockSize, b.length - off);
				byte[] realData = cipher.doFinal(b, off, len);
				os.write(realData);
				off = off + len;
			}
			
			String stringEncoding = encoded.getString(PemBlock.STRING_ENCODING, "utf-8");
			return new String(os.toByteArray(), stringEncoding);
		
		} catch (Exception e) {
			throw new MException(e);
		}
	}

	
}
