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
package de.mhus.app.vault.core.services;

import java.io.PrintStream;

import org.osgi.service.component.annotations.Component;

import de.mhus.app.vault.api.ifc.SecretContent;
import de.mhus.app.vault.api.ifc.SecretGenerator;
import de.mhus.app.vault.api.model.VaultGroup;
import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.util.SecureString;

@Component(property = "name=password")
public class PasswordGenerator implements SecretGenerator {

    @Override
    public SecretContent generateSecret(VaultGroup group, IProperties param) {
        IReadProperties config = group.getSecretGeneratorConfig();
        int min = config.getInt("min", 6);
        int max = config.getInt("max", 20);
        boolean upper = config.getBoolean("upper", true);
        boolean numbers = config.getBoolean("numbers", true);
        boolean specials = config.getBoolean("specials", true);
        SecureString pw = new SecureString(MPassword.generate(min, max, upper, numbers, specials));
        return new SecretContent(pw, null);
    }

    @Override
    public void test(PrintStream out, VaultGroup group, IProperties properties) {}
}
