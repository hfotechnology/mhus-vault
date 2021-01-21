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
package de.mhus.app.vault.client;

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
