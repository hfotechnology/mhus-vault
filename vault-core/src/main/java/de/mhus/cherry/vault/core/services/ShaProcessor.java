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

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.osgi.service.component.annotations.Component;

import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.ifc.TargetProcessor;
import de.mhus.cherry.vault.api.model.WritableEntry;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.M;
import de.mhus.lib.core.crypt.MRandom;
import de.mhus.lib.core.crypt.pem.PemBlock;
import de.mhus.lib.core.crypt.pem.PemBlockList;
import de.mhus.lib.core.crypt.pem.PemBlockModel;
import de.mhus.lib.errors.MException;

@Component(property="name=hash.sha")
public class ShaProcessor implements TargetProcessor {

	@Override
	public void process(IProperties properties, IReadProperties processorConfig, SecretContent secret,
	        WritableEntry entry) throws MException {
		
		try {
			
			MRandom random = M.l(MRandom.class);
            String salt = null;
            if (processorConfig.containsKey("salt"))
                salt = processorConfig.getString("salt");
            else
                salt = "" + random.getChar() + random.getChar();
			
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salt.getBytes("utf-8"));
			md.update(secret.getContent().value().getBytes("utf-8"));
			byte[] digest = md.digest();
			String cs = Base64.getEncoder().encodeToString(digest);
			
			PemBlockList result = new PemBlockList();
			PemBlockModel block = new PemBlockModel(PemBlock.BLOCK_HASH, cs);
			block.setString(PemBlock.METHOD, "SHA-256");
			block.setString("Salt", salt);
			block.setString(PemBlock.STRING_ENCODING, "utf-8");
			result.add(block);
			
			SignerUtil.sign(result, processorConfig, cs);

			entry.setSecret(result.toString());

		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new MException(e);
		}
		
	}

    @Override
    public void test(PrintStream out, IProperties properties, IReadProperties processorConfig) throws Exception {
        if (processorConfig.containsKey("salt"))
            out.println("Contains salt: " + processorConfig.getString("salt"));
        SignerUtil.test(out, properties, processorConfig);
    }

}
