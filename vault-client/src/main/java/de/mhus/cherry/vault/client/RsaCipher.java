/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.client;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.crypto.Cipher;

import de.mhus.lib.core.crypt.Blowfish;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockModel;
import de.mhus.lib.core.crypt.pem.PemPriv;
import de.mhus.lib.core.crypt.pem.PemPub;
import de.mhus.lib.errors.MException;

public class RsaCipher {

    private static final String TRANSFORMATION_RSA = "RSA/ECB/PKCS1Padding";
    private static final String ALGORITHM_RSA = "RSA";

    public static PemBlock encode(PemPub key, String content) throws MException {
        try {
            byte[] encKey = key.getBytesBlock();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            String stringEncoding = "utf-8";
            byte[] b = content.getBytes(stringEncoding);
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            int length = key.getInt(PemBlock.LENGTH, 1024);
            int blockSize = length == 512 ? 53 : 117;

            int off = 0;
            while (off < b.length) {
                int len = Math.min(blockSize, b.length - off);
                byte[] cipherData = cipher.doFinal(b, off, len);
                os.write(cipherData);
                off = off + len;
            }

            PemBlockModel out = new PemBlockModel(PemBlock.BLOCK_CIPHER, os.toByteArray());
            out.set(PemBlock.METHOD, "RSA-1");
            out.set(PemBlock.STRING_ENCODING, stringEncoding);
            if (key.isProperty(PemBlock.IDENT))
                out.set(PemBlock.PUB_ID, key.getString(PemBlock.IDENT));
            if (key.isProperty(PemBlock.PRIV_ID))
                out.set(PemBlock.PRIV_ID, key.getString(PemBlock.PRIV_ID));
            out.set(PemBlock.CREATED, new Date());

            return out;

        } catch (Throwable t) {
            throw new MException(t);
        }
    }

    public static String decode(PemPriv key, PemBlock encoded, String passphrase)
            throws MException {
        try {
            byte[] encKey = key.getBytesBlock();
            if (passphrase != null) encKey = Blowfish.decrypt(encKey, passphrase);
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA);
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
