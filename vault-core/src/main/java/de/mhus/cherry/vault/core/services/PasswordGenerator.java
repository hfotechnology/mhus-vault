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

import org.osgi.service.component.annotations.Component;
import de.mhus.cherry.vault.api.ifc.SecretContent;
import de.mhus.cherry.vault.api.ifc.SecretGenerator;
import de.mhus.cherry.vault.api.model.VaultGroup;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.util.SecureString;

@Component(property="name=password")
public class PasswordGenerator implements SecretGenerator {

	@Override
	public SecretContent generateSecret(VaultGroup group, IProperties param) {
		int min          = param.getInt("password.min", 6);
		int max          = param.getInt("password.max", 20);
		boolean upper    = param.getBoolean("password.upper", true);
		boolean numbers  = param.getBoolean("password.numbers", true);
		boolean specials = param.getBoolean("password.specials", true);
		SecureString pw = new SecureString( MPassword.generate(min, max, upper, numbers, specials) );
		return new SecretContent(pw,null);
	}

}
